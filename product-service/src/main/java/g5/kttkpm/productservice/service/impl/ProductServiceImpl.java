package g5.kttkpm.productservice.service.impl;

import g5.kttkpm.productservice.dto.CategoryDTO;
import g5.kttkpm.productservice.dto.ProductUpdateRequest;
import g5.kttkpm.productservice.model.Product;
import g5.kttkpm.productservice.model.ProductImage;
import g5.kttkpm.productservice.repo.ProductImageRepository;
import g5.kttkpm.productservice.repo.ProductRepository;
import g5.kttkpm.productservice.client.CategoryClient;
import g5.kttkpm.productservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
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
    private final ProductImageRepository productImageRepository; // Added repository for image handling
    
    @Autowired
    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryClient categoryClient,
                              MongoTemplate mongoTemplate,
                              ProductImageRepository productImageRepository) {
        this.productRepository = productRepository;
        this.categoryClient = categoryClient;
        this.mongoTemplate = mongoTemplate;
        this.productImageRepository = productImageRepository;
    }
    
    // Basic CRUD Operations
    
    @Override
    public Page<Product> getAllProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);
        fetchAndSetImagesForProducts(products.getContent());
        return products;
    }
    
    @Override
    public Product getProductById(String id) {
        Product product = productRepository.findByProductId(id)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        // Fetch and set images
        fetchAndSetImagesForProduct(product);
        return product;
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
        
        if (product.getImageUrls() == null) {
            product.setImageUrls(new ArrayList<>());
        }
        
        // Track initial price
        if (product.getCurrentPrice() != null) {
            Product.PriceHistory initialPrice = Product.PriceHistory.builder()
                .oldPrice(null) // No old price for initial pricing
                .newPrice(product.getCurrentPrice())
                .timestamp(now)
                .changedBy("system")
                .changeReason(Product.PriceChangeReason.INITIAL_PRICING)
                .build();
            product.getPriceHistory().add(initialPrice);
        }
        
        // Track initial quantity
        if (product.getTotalQuantity() != null) {
            Product.QuantityHistory initialQuantity = Product.QuantityHistory.builder()
                .oldQuantity(null) // No old quantity for initial stock
                .newQuantity(product.getTotalQuantity())
                .timestamp(now)
                .changedBy("system")
                .changeReason(Product.QuantityChangeReason.INITIAL_STOCK)
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
        
        Product savedProduct = productRepository.save(product);
        
        // Create ProductImage entry if there are image URLs
        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            ProductImage productImage = ProductImage.builder()
                .productId(savedProduct.getProductId())
                .imageUrl(product.getImageUrls())
                .build();
            productImageRepository.save(productImage);
        }
        
        return savedProduct;
    }
    
    @Override
    public Product updateProduct(String id, Product productDetails) {
        Product product = getProductById(id);
        
        // Update basic info
        product.setName(productDetails.getName());
        product.setSku(productDetails.getSku());
        product.setDescription(productDetails.getDescription());
        product.setBrand(productDetails.getBrand());
        product.setThumbnailUrl(productDetails.getThumbnailUrl());
        
        // Update image URLs if provided
        if (productDetails.getImageUrls() != null) {
            product.setImageUrls(productDetails.getImageUrls());
            
            // Update or create product image entry
            Optional<ProductImage> existingImages = productImageRepository.findByProductId(product.getProductId());
            if (existingImages.isPresent()) {
                ProductImage productImage = existingImages.get();
                productImage.setImageUrl(productDetails.getImageUrls());
                productImageRepository.save(productImage);
            } else if (!productDetails.getImageUrls().isEmpty()) {
                ProductImage productImage = ProductImage.builder()
                    .productId(product.getProductId())
                    .imageUrl(productDetails.getImageUrls())
                    .build();
                productImageRepository.save(productImage);
            }
        }
        
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
        
        // Delete associated images
        productImageRepository.findByProductId(product.getProductId())
            .ifPresent(productImageRepository::delete);
        
        productRepository.delete(product);
    }
    
    // Search Operations
    
    @Override
    public Page<Product> searchProducts(String name, String sku, String category, String brand,
                                        BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Query query = new Query().with(pageable);
        List<Criteria> criteria = new ArrayList<>();
        
        // Case-insensitive partial matching for text fields
        if (name != null && !name.isEmpty()) {
            // Using regex with "i" flag for case-insensitive searching
            criteria.add(Criteria.where("name").regex(".*" + name + ".*", "i"));
        }
        
        if (sku != null && !sku.isEmpty()) {
            criteria.add(Criteria.where("sku").regex(".*" + sku + ".*", "i"));
        }
        
        if (category != null && !category.isEmpty()) {
            Criteria categoryCriteria = new Criteria().orOperator(
                Criteria.where("mainCategoryId").regex(".*" + category + ".*", "i"),
                // Search in additional categories array too
                Criteria.where("additionalCategories").regex(".*" + category + ".*", "i")
            );
            criteria.add(categoryCriteria);
        }
        
        if (brand != null && !brand.isEmpty()) {
            criteria.add(Criteria.where("brand").regex(".*" + brand + ".*", "i"));
        }
        
        // Add price range search capability
        if (minPrice != null) {
            criteria.add(Criteria.where("currentPrice").gte(minPrice));
        }
        
        if (maxPrice != null) {
            criteria.add(Criteria.where("currentPrice").lte(maxPrice));
        }
        
        if (!criteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
        }
        
        List<Product> products = mongoTemplate.find(query, Product.class);
        fetchAndSetImagesForProducts(products);
        
        return PageableExecutionUtils.getPage(
            products,
            pageable,
            () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Product.class)
        );
    }
    
    @Override
    public Page<Product> searchProductsAdvanced(Map<String, Object> searchCriteria, Pageable pageable) {
        Query query = new Query().with(pageable);
        List<Criteria> criteriaList = new ArrayList<>();
        
        // Process text search fields with fuzzy matching
        processTextSearchField(searchCriteria, "name", criteriaList);
        processTextSearchField(searchCriteria, "sku", criteriaList);
        processTextSearchField(searchCriteria, "brand", criteriaList);
        processTextSearchField(searchCriteria, "description", criteriaList);
        
        // Handle category searching
        if (searchCriteria.containsKey("category")) {
            String categoryValue = searchCriteria.get("category").toString();
            Criteria categoryCriteria = new Criteria().orOperator(
                Criteria.where("mainCategoryId").is(categoryValue),
                Criteria.where("additionalCategories").in(categoryValue)
            );
            criteriaList.add(categoryCriteria);
        }
        
        // Handle fuzzy category search
        if (searchCriteria.containsKey("categoryText")) {
            String categoryText = searchCriteria.get("categoryText").toString();
            Criteria categoryCriteria = new Criteria().orOperator(
                Criteria.where("mainCategoryId").regex(".*" + categoryText + ".*", "i"),
                Criteria.where("additionalCategories").regex(".*" + categoryText + ".*", "i")
            );
            criteriaList.add(categoryCriteria);
        }
        
        // Handle price range
        if (searchCriteria.containsKey("minPrice")) {
            BigDecimal minPrice = new BigDecimal(searchCriteria.get("minPrice").toString());
            criteriaList.add(Criteria.where("currentPrice").gte(minPrice));
        }
        
        if (searchCriteria.containsKey("maxPrice")) {
            BigDecimal maxPrice = new BigDecimal(searchCriteria.get("maxPrice").toString());
            criteriaList.add(Criteria.where("currentPrice").lte(maxPrice));
        }
        
        // Handle status filter
        if (searchCriteria.containsKey("status")) {
            Product.ProductStatus status = Product.ProductStatus.valueOf(searchCriteria.get("status").toString());
            criteriaList.add(Criteria.where("status").is(status));
        }
        
        // Handle inventory filter
        if (searchCriteria.containsKey("inStock") && Boolean.TRUE.equals(searchCriteria.get("inStock"))) {
            criteriaList.add(Criteria.where("availableQuantity").gt(0));
        }
        
        // Handle additional attributes search
        searchCriteria.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith("attr."))
            .forEach(entry -> {
                String attributeKey = entry.getKey().substring(5); // Remove "attr." prefix
                criteriaList.add(Criteria.where("additionalAttributes." + attributeKey).is(entry.getValue()));
            });
        
        // Apply all criteria as AND condition
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }
        
        List<Product> products = mongoTemplate.find(query, Product.class);
        fetchAndSetImagesForProducts(products);
        
        return PageableExecutionUtils.getPage(
            products,
            pageable,
            () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Product.class)
        );
    }
    
    // Helper method to process text fields with fuzzy search
    private void processTextSearchField(Map<String, Object> searchCriteria, String fieldName, List<Criteria> criteriaList) {
        if (searchCriteria.containsKey(fieldName)) {
            String value = searchCriteria.get(fieldName).toString();
            if (value != null && !value.isEmpty()) {
                // Build regex pattern with word boundaries for more accurate matching
                // This makes the search more intelligent by matching whole words or parts of words
                String[] terms = value.split("\\s+");
                List<Criteria> termCriteria = new ArrayList<>();
                
                for (String term : terms) {
                    if (!term.isEmpty()) {
                        // Use case-insensitive regex pattern
                        termCriteria.add(Criteria.where(fieldName).regex(".*" + term + ".*", "i"));
                    }
                }
                
                if (!termCriteria.isEmpty()) {
                    // If multiple terms, require any term to match (OR condition)
                    criteriaList.add(new Criteria().orOperator(termCriteria.toArray(new Criteria[0])));
                }
            }
        }
    }
    
    @Override
    public Page<Product> autoCompleteSearch(String query, Pageable pageable) {
        if (query == null || query.isEmpty()) {
            return Page.empty(pageable);
        }
        
        // Normalize the query
        String normalizedQuery = query.trim().toLowerCase();
        
        // Create a text index query for more natural language searching
        TextCriteria textCriteria = TextCriteria.forDefaultLanguage()
            .matchingAny(normalizedQuery);
        
        // If MongoDB text index is set up, we can use it for better performance
        try {
            Query textQuery = TextQuery.queryText(textCriteria)
                .sortByScore()
                .with(pageable);
            
            List<Product> products = mongoTemplate.find(textQuery, Product.class);
            fetchAndSetImagesForProducts(products);
            
            long count = mongoTemplate.count(TextQuery.queryText(textCriteria), Product.class);
            
            return new PageImpl<>(products, pageable, count);
        } catch (Exception e) {
            // Fallback to regex search if text index is not available
            System.err.println("Text search failed, falling back to regex: " + e.getMessage());
            
            // Build a query that searches across multiple fields
            List<Criteria> fieldCriteria = Arrays.asList(
                Criteria.where("name").regex(".*" + normalizedQuery + ".*", "i"),
                Criteria.where("sku").regex(".*" + normalizedQuery + ".*", "i"),
                Criteria.where("brand").regex(".*" + normalizedQuery + ".*", "i"),
                Criteria.where("description").regex(".*" + normalizedQuery + ".*", "i")
            );
            
            Criteria combinedCriteria = new Criteria().orOperator(
                fieldCriteria.toArray(new Criteria[0])
            );
            
            Query mongoQuery = new Query(combinedCriteria).with(pageable);
            List<Product> products = mongoTemplate.find(mongoQuery, Product.class);
            fetchAndSetImagesForProducts(products);
            
            return PageableExecutionUtils.getPage(
                products,
                pageable,
                () -> mongoTemplate.count(Query.of(mongoQuery).limit(-1).skip(-1), Product.class)
            );
        }
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
        
        // Update total quantity and maintain history
        product.updateQuantity(newQuantity, reason, changedBy);
        
        // Handle different scenarios based on reason
        if (reason == Product.QuantityChangeReason.ORDER_PLACEMENT) {
            // For order placement: increase reserved quantity, decrease available quantity
            int orderQuantity = -difference; // Convert the negative difference to positive order quantity
            product.setReservedQuantity(product.getReservedQuantity() + orderQuantity);
            // Available quantity is already reduced by the difference calculation below
        } else if (reason == Product.QuantityChangeReason.ORDER_CANCELLATION) {
            // For order cancellation: decrease reserved quantity, increase available quantity
            int cancelQuantity = difference; // The positive difference is the cancel quantity
            product.setReservedQuantity(Math.max(0, product.getReservedQuantity() - cancelQuantity));
            // Available quantity is already increased by the difference calculation below
        }
        
        // Update available quantity based on the calculated difference
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
    
    // New specialized methods for order operations
    
    @Override
    public Product reserveProductQuantity(String id, Integer quantity, String orderId) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to reserve must be positive");
        }
        
        Product product = getProductById(id);
        
        // Check if there's enough available quantity
        if (product.getAvailableQuantity() < quantity) {
            throw new RuntimeException("Not enough available quantity. Available: " +
                product.getAvailableQuantity() + ", Requested: " + quantity);
        }
        
        // Decrease available quantity
        product.setAvailableQuantity(product.getAvailableQuantity() - quantity);
        
        // Increase reserved quantity
        product.setReservedQuantity(product.getReservedQuantity() + quantity);
        
        // Update total quantity history for tracking
        int newTotalQuantity = product.getTotalQuantity(); // Total remains the same
        product.updateQuantity(newTotalQuantity, Product.QuantityChangeReason.ORDER_PLACEMENT, "order-" + orderId);
        
        // Update status if needed
        if (product.getAvailableQuantity() <= 0) {
            product.setStatus(Product.ProductStatus.OUT_OF_STOCK);
        }
        
        product.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }
    
    @Override
    public Product cancelReservation(String id, Integer quantity, String orderId) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to cancel must be positive");
        }
        
        Product product = getProductById(id);
        
        // Ensure we don't decrease reserved quantity below zero
        int quantityToCancel = Math.min(quantity, product.getReservedQuantity());
        
        // Decrease reserved quantity
        product.setReservedQuantity(product.getReservedQuantity() - quantityToCancel);
        
        // Increase available quantity
        product.setAvailableQuantity(product.getAvailableQuantity() + quantityToCancel);
        
        // Update total quantity history for tracking
        int newTotalQuantity = product.getTotalQuantity(); // Total remains the same
        product.updateQuantity(newTotalQuantity, Product.QuantityChangeReason.ORDER_CANCELLATION, "cancel-" + orderId);
        
        // Update status if needed
        if (product.getStatus() == Product.ProductStatus.OUT_OF_STOCK && product.getAvailableQuantity() > 0) {
            product.setStatus(Product.ProductStatus.ACTIVE);
        }
        
        product.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }
    
    @Override
    public Product completeOrder(String id, Integer quantity, String orderId) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to complete must be positive");
        }
        
        Product product = getProductById(id);
        
        // Ensure we don't decrease reserved quantity below zero
        int quantityToComplete = Math.min(quantity, product.getReservedQuantity());
        
        // Decrease reserved quantity (items are now sold)
        product.setReservedQuantity(product.getReservedQuantity() - quantityToComplete);
        
        // Decrease total quantity (items have left inventory)
        int newTotalQuantity = product.getTotalQuantity() - quantityToComplete;
        product.updateQuantity(newTotalQuantity, Product.QuantityChangeReason.ORDER_FULFILLED, "complete-" + orderId);
        
        product.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }
    
    @Override
    public Product returnItems(String id, Integer quantity, String orderId) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to return must be positive");
        }
        
        Product product = getProductById(id);
        
        // Increase total quantity
        int newTotalQuantity = product.getTotalQuantity() + quantity;
        
        // Increase available quantity
        product.setAvailableQuantity(product.getAvailableQuantity() + quantity);
        
        // Update total quantity history
        product.updateQuantity(newTotalQuantity, Product.QuantityChangeReason.RETURN, "return-" + orderId);
        
        // Update status if needed
        if (product.getStatus() == Product.ProductStatus.OUT_OF_STOCK && product.getAvailableQuantity() > 0) {
            product.setStatus(Product.ProductStatus.ACTIVE);
        }
        
        product.setUpdatedAt(LocalDateTime.now());
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
                    Criteria.where("$eq").is(categoryId)
                )
            )
        ).with(pageable);
        
        List<Product> products = mongoTemplate.find(query, Product.class);
        fetchAndSetImagesForProducts(products);
        
        return PageableExecutionUtils.getPage(
            products,
            pageable,
            () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Product.class)
        );
    }
    
    @Override
    public Page<Product> getProductsByBrand(String brand, Pageable pageable) {
        Page<Product> products = productRepository.findByBrandIgnoreCase(brand, pageable);
        fetchAndSetImagesForProducts(products.getContent());
        return products;
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
        Page<Product> products = productRepository.findByMainCategoryIdOrAdditionalCategoriesContaining(categoryId, categoryId, pageable);
        fetchAndSetImagesForProducts(products.getContent());
        return products;
    }
    
    // Helper methods for image handling
    
    private void fetchAndSetImagesForProducts(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return;
        }
        
        // Get all product IDs
        List<String> productIds = products.stream()
            .map(Product::getProductId)
            .collect(Collectors.toList());
        
        // Fetch all image records in one go
        List<ProductImage> productImages = productImageRepository.findAllByProductIdIn(productIds);
        
        // Create a map for quick lookup
        Map<String, List<String>> productImageMap = productImages.stream()
            .collect(Collectors.toMap(
                ProductImage::getProductId,
                ProductImage::getImageUrl,
                (existing, replacement) -> existing // In case of duplicate keys
            ));
        
        // Set images for each product
        products.forEach(product -> {
            List<String> images = productImageMap.get(product.getProductId());
            product.setImageUrls(images != null ? images : new ArrayList<>());
        });
    }
    
    private void fetchAndSetImagesForProduct(Product product) {
        if (product == null) {
            return;
        }
        
        productImageRepository.findByProductId(product.getProductId())
            .ifPresent(productImage ->
                product.setImageUrls(productImage.getImageUrl())
            );
    }
    
    @Override
    public Product updateProductUnified(String id, ProductUpdateRequest updateRequest) {
        Product product = getProductById(id);
        
        // Xử lý loại cập nhật dựa trên operation
        ProductUpdateRequest.UpdateOperationType operation = updateRequest.getOperation();
        if (operation == null) {
            operation = ProductUpdateRequest.UpdateOperationType.ALL; // Mặc định cập nhật tất cả
        }
        
        // Xử lý từng loại cập nhật
        switch (operation) {
            case ALL:
                updateBasicInfo(product, updateRequest);
                updatePrice(product, updateRequest);
                updateQuantity(product, updateRequest);
                updateStatus(product, updateRequest);
                updateCategories(product, updateRequest);
                updateAttributes(product, updateRequest);
                break;
            case BASIC_INFO:
                updateBasicInfo(product, updateRequest);
                break;
            case PRICE:
                updatePrice(product, updateRequest);
                break;
            case QUANTITY:
                updateQuantity(product, updateRequest);
                break;
            case STATUS:
                updateStatus(product, updateRequest);
                break;
            case CATEGORIES:
                updateCategories(product, updateRequest);
                break;
            case ATTRIBUTES:
                updateAttributes(product, updateRequest);
                break;
        }
        
        product.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }
    
    // Các phương thức hỗ trợ xử lý từng phần
    private void updateBasicInfo(Product product, ProductUpdateRequest request) {
        if (request.getName() != null) product.setName(request.getName());
        if (request.getSku() != null) product.setSku(request.getSku());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getBrand() != null) product.setBrand(request.getBrand());
        if (request.getThumbnailUrl() != null) product.setThumbnailUrl(request.getThumbnailUrl());
        
        // Cập nhật hình ảnh nếu được cung cấp
        if (request.getImageUrls() != null) {
            product.setImageUrls(request.getImageUrls());
            
            // Cập nhật hoặc tạo mới bản ghi hình ảnh sản phẩm
            Optional<ProductImage> existingImages = productImageRepository.findByProductId(product.getProductId());
            if (existingImages.isPresent()) {
                ProductImage productImage = existingImages.get();
                productImage.setImageUrl(request.getImageUrls());
                productImageRepository.save(productImage);
            } else if (!request.getImageUrls().isEmpty()) {
                ProductImage productImage = ProductImage.builder()
                    .productId(product.getProductId())
                    .imageUrl(request.getImageUrls())
                    .build();
                productImageRepository.save(productImage);
            }
        }
    }
    
    private void updatePrice(Product product, ProductUpdateRequest request) {
        if (request.getCurrentPrice() != null) {
            // Sử dụng phương thức hiện có cho theo dõi lịch sử
            Product.PriceChangeReason reason = request.getPriceChangeReason() != null ?
                request.getPriceChangeReason() : Product.PriceChangeReason.MANUAL_UPDATE;
            
            String changedBy = request.getPriceChangedBy() != null ?
                request.getPriceChangedBy() : "system";
            
            product.updatePrice(request.getCurrentPrice(), reason, changedBy);
        }
    }
    
    private void updateQuantity(Product product, ProductUpdateRequest request) {
        if (request.getTotalQuantity() != null) {
            // Sử dụng phương thức hiện có cho theo dõi lịch sử
            Product.QuantityChangeReason reason = request.getQuantityChangeReason() != null ?
                request.getQuantityChangeReason() : Product.QuantityChangeReason.MANUAL_ADJUSTMENT;
            
            String changedBy = request.getQuantityChangedBy() != null ?
                request.getQuantityChangedBy() : "system";
            
            // Tính toán sự khác biệt để cập nhật số lượng có sẵn
            int difference = request.getTotalQuantity() - product.getTotalQuantity();
            
            // Cập nhật tổng số lượng
            product.updateQuantity(request.getTotalQuantity(), reason, changedBy);
            
            // Cập nhật số lượng có sẵn
            int newAvailable = product.getAvailableQuantity() + difference;
            product.setAvailableQuantity(Math.max(0, newAvailable));
            
            // Tự động cập nhật trạng thái nếu sản phẩm hết hàng
            if (product.getAvailableQuantity() <= 0) {
                product.setStatus(Product.ProductStatus.OUT_OF_STOCK);
            } else if (product.getStatus() == Product.ProductStatus.OUT_OF_STOCK) {
                product.setStatus(Product.ProductStatus.ACTIVE);
            }
        }
    }
    
    private void updateStatus(Product product, ProductUpdateRequest request) {
        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }
    }
    
    private void updateCategories(Product product, ProductUpdateRequest request) {
        // Cập nhật danh mục chính nếu được cung cấp
        if (request.getMainCategoryId() != null) {
            // Kiểm tra danh mục tồn tại
            CategoryDTO category = categoryClient.getCategoryById(request.getMainCategoryId());
            if (category == null) {
                throw new RuntimeException("Category not found with id: " + request.getMainCategoryId());
            }
            product.setMainCategory(request.getMainCategoryId());
        }
        
        // Cập nhật danh mục bổ sung nếu được cung cấp
        if (request.getAdditionalCategories() != null) {
            // Xóa tất cả danh mục hiện có trước
            if (product.getAdditionalCategories() == null) {
                product.setAdditionalCategories(new ArrayList<>());
            } else {
                product.getAdditionalCategories().clear();
            }
            
            // Thêm các danh mục mới và kiểm tra tính hợp lệ
            for (String categoryId : request.getAdditionalCategories()) {
                CategoryDTO category = categoryClient.getCategoryById(categoryId);
                if (category == null) {
                    throw new RuntimeException("Category not found with id: " + categoryId);
                }
                product.addCategory(categoryId);
            }
        }
    }
    
    private void updateAttributes(Product product, ProductUpdateRequest request) {
        if (request.getAdditionalAttributes() != null) {
            // Khởi tạo nếu null
            if (product.getAdditionalAttributes() == null) {
                product.setAdditionalAttributes(new HashMap<>());
            }
            
            // Cập nhật hoặc thêm thuộc tính
            product.getAdditionalAttributes().putAll(request.getAdditionalAttributes());
        }
    }
}
