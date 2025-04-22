package g5.kttkpm.productservice.service.impl;

import g5.kttkpm.productservice.dto.CategoryDTO;
import g5.kttkpm.productservice.model.Product;
import g5.kttkpm.productservice.repo.ProductRepository;
import g5.kttkpm.productservice.client.CategoryClient;
import g5.kttkpm.productservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    
    private final ProductRepository productRepository;
    private final CategoryClient categoryClient;
    private final MongoTemplate mongoTemplate;
    
    @Autowired
    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryClient categoryClient,
                              MongoTemplate mongoTemplate) {
        this.productRepository = productRepository;
        this.categoryClient = categoryClient;
        this.mongoTemplate = mongoTemplate;
    }
    
    // Basic CRUD Operations
    
    @Override
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }
    
    @Override
    public Product getProductById(String id) {
        return productRepository.findByProductId(id)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }
    
    @Override
    public Product createProduct(Product product) {
        // Set initial values
        if (product.getProductId() == null || product.getProductId().isEmpty()) {
            product.setProductId(UUID.randomUUID().toString());
        }
        
        LocalDateTime now = LocalDateTime.now();
        product.setCreatedAt(now);
        product.setUpdatedAt(now);
        
        // Initialize collections if null
        if (product.getPriceHistory() == null) {
            product.setPriceHistory(new ArrayList<>());
        }
        
        if (product.getQuantityHistory() == null) {
            product.setQuantityHistory(new ArrayList<>());
        }
        
        if (product.getAdditionalAttributes() == null) {
            product.setAdditionalAttributes(new HashMap<>());
        }
        
        if (product.getAdditionalCategories() == null) {
            product.setAdditionalCategories(new ArrayList<>());
        }
        
        // Track initial price
        if (product.getCurrentPrice() != null) {
            Product.PriceHistory initialPrice = Product.PriceHistory.builder()
                .price(product.getCurrentPrice())
                .changedAt(now)
                .changedBy("system")
                .reason(Product.PriceChangeReason.INITIAL_PRICING)
                .build();
            product.getPriceHistory().add(initialPrice);
        }
        
        // Track initial quantity
        if (product.getTotalQuantity() != null) {
            Product.QuantityHistory initialQuantity = Product.QuantityHistory.builder()
                .quantity(product.getTotalQuantity())
                .changedAt(now)
                .changedBy("system")
                .reason(Product.QuantityChangeReason.INITIAL_STOCK)
                .build();
            product.getQuantityHistory().add(initialQuantity);
        }
        
        // Initialize available quantity if not set
        if (product.getAvailableQuantity() == null && product.getTotalQuantity() != null) {
            product.setAvailableQuantity(product.getTotalQuantity());
        }
        
        // Initialize reserved quantity if not set
        if (product.getReservedQuantity() == null) {
            product.setReservedQuantity(0);
        }
        
        return productRepository.save(product);
    }
    
    @Override
    public Product updateProduct(String id, Product productDetails) {
        Product product = getProductById(id);
        
        // Update basic info
        product.setName(productDetails.getName());
        product.setSku(productDetails.getSku());
        product.setDescription(productDetails.getDescription());
        product.setBrand(productDetails.getBrand());
        
        // Don't update prices and quantities directly to preserve history
        
        // Update status if provided
        if (productDetails.getStatus() != null) {
            product.setStatus(productDetails.getStatus());
        }
        
        // Update additional attributes if provided
        if (productDetails.getAdditionalAttributes() != null) {
            product.setAdditionalAttributes(productDetails.getAdditionalAttributes());
        }
        
        product.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }
    
    @Override
    public void deleteProduct(String id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }
    
    // Search Operations
    
    @Override
    public Page<Product> searchProducts(String name, String sku, String category, String brand, Pageable pageable) {
        Query query = new Query().with(pageable);
        List<Criteria> criteria = new ArrayList<>();
        
        if (name != null && !name.isEmpty()) {
            criteria.add(Criteria.where("name").regex(name, "i"));
        }
        
        if (sku != null && !sku.isEmpty()) {
            criteria.add(Criteria.where("sku").regex(sku, "i"));
        }
        
        if (category != null && !category.isEmpty()) {
            criteria.add(Criteria.where("category").regex(category, "i"));
        }
        
        if (brand != null && !brand.isEmpty()) {
            criteria.add(Criteria.where("brand").regex(brand, "i"));
        }
        
        if (!criteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
        }
        
        List<Product> products = mongoTemplate.find(query, Product.class);
        
        return PageableExecutionUtils.getPage(
            products,
            pageable,
            () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Product.class)
        );
    }
    
    // Price Management
    
    @Override
    public Product updateProductPrice(String id, BigDecimal newPrice, Product.PriceChangeReason reason, String changedBy) {
        Product product = getProductById(id);
        product.updatePrice(newPrice, reason, changedBy);
        return productRepository.save(product);
    }
    
    @Override
    public List<Product.PriceHistory> getPriceHistory(String id) {
        Product product = getProductById(id);
        return product.getPriceHistory();
    }
    
    // Inventory Management
    
    @Override
    public Product updateProductInventory(String id, Integer newQuantity, Product.QuantityChangeReason reason, String changedBy) {
        Product product = getProductById(id);
        
        // Calculate the difference to update available quantity
        int difference = newQuantity - product.getTotalQuantity();
        
        // Update total quantity
        product.updateQuantity(newQuantity, reason, changedBy);
        
        // Update available quantity
        int newAvailable = product.getAvailableQuantity() + difference;
        product.setAvailableQuantity(Math.max(0, newAvailable));
        
        // Auto-update status if product is out of stock
        if (product.getAvailableQuantity() <= 0) {
            product.setStatus(Product.ProductStatus.OUT_OF_STOCK);
        } else if (product.getStatus() == Product.ProductStatus.OUT_OF_STOCK) {
            product.setStatus(Product.ProductStatus.ACTIVE);
        }
        
        return productRepository.save(product);
    }
    
    @Override
    public List<Product.QuantityHistory> getQuantityHistory(String id) {
        Product product = getProductById(id);
        return product.getQuantityHistory();
    }
    
    // Category Management
    
    @Override
    public Product setMainCategory(String productId, String categoryId) {
        Product product = getProductById(productId);
        
        // Fetch the category from Category service
        CategoryDTO category = categoryClient.getCategoryById(categoryId);
        if (category == null) {
            throw new RuntimeException("Category not found with id: " + categoryId);
        }
        
        // Using the nested class we added to Product
        product.setMainCategory(categoryId);
        product.setUpdatedAt(LocalDateTime.now());
        
        return productRepository.save(product);
    }
    
    @Override
    public Product addCategory(String productId, String categoryId) {
        Product product = getProductById(productId);
        
        // Fetch the category from Category service
        CategoryDTO category = categoryClient.getCategoryById(categoryId);
        if (category == null) {
            throw new RuntimeException("Category not found with id: " + categoryId);
        }
        
        // Using the nested class we added to Product
        product.addCategory(categoryId);
        product.setUpdatedAt(LocalDateTime.now());
        
        return productRepository.save(product);
    }
    
    @Override
    public Product removeCategory(String productId, String categoryId) {
        Product product = getProductById(productId);
        
        product.removeCategory(categoryId);
        product.setUpdatedAt(LocalDateTime.now());
        
        return productRepository.save(product);
    }
    
    @Override
    public List<CategoryDTO> getProductCategories(String productId) {
        Product product = getProductById(productId);
        
        List<String> categoryIds = new ArrayList<>();
        
        // Add main category if present
        if (product.getMainCategoryId() != null) {
            categoryIds.add(product.getMainCategoryId());
        }
        
        // Add additional categories
        if (product.getAdditionalCategories() != null) {
            categoryIds.addAll(product.getAdditionalCategories());
        }
        
        // Fetch all categories in parallel
        return categoryIds.stream()
            .map(id -> {
                try {
                    return categoryClient.getCategoryById(id);
                } catch (Exception e) {
                    // Log error and continue
                    System.err.println("Error fetching category " + id + ": " + e.getMessage());
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    // Status Management
    
    @Override
    public Product updateProductStatus(String id, Product.ProductStatus status) {
        Product product = getProductById(id);
        product.setStatus(status);
        product.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }
    
    // Batch Operations
    
    @Override
    public List<Product> createProducts(List<Product> products) {
        // Process each product through the creation method to ensure proper initialization
        return products.stream()
            .map(this::createProduct)
            .collect(Collectors.toList());
    }
    
    @Override
    public Page<Product> getProductsByCategory(String categoryId, Pageable pageable) {
        // Find products by main category
        Query query = new Query(
            new Criteria().orOperator(
                Criteria.where("mainCategoryId").is(categoryId),
                Criteria.where("additionalCategories").elemMatch(
                    Criteria.where("categoryId").is(categoryId)
                )
            )
        ).with(pageable);
        
        List<Product> products = mongoTemplate.find(query, Product.class);
        
        return PageableExecutionUtils.getPage(
            products,
            pageable,
            () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Product.class)
        );
    }
    
    @Override
    public Page<Product> getProductsByBrand(String brand, Pageable pageable) {
        return productRepository.findByBrandIgnoreCase(brand, pageable);
    }
    
    // Additional Attributes Management
    
    @Override
    public Product updateProductAttributes(String id, Map<String, Object> attributes) {
        Product product = getProductById(id);
        
        // Initialize if null
        if (product.getAdditionalAttributes() == null) {
            product.setAdditionalAttributes(new HashMap<>());
        }
        
        // Update or add attributes
        product.getAdditionalAttributes().putAll(attributes);
        product.setUpdatedAt(LocalDateTime.now());
        
        return productRepository.save(product);
    }
    
    @Override
    public Product removeProductAttribute(String id, String key) {
        Product product = getProductById(id);
        
        if (product.getAdditionalAttributes() != null) {
            product.getAdditionalAttributes().remove(key);
            product.setUpdatedAt(LocalDateTime.now());
        }
        
        return productRepository.save(product);
    }
    
    @Override
    public Page<Product> getAllProductsByCategoryId(String categoryId, Pageable pageable) {
        return productRepository.findByMainCategoryIdOrAdditionalCategoriesContaining(categoryId, categoryId, pageable);
    }
}
