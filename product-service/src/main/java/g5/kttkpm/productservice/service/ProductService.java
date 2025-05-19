package g5.kttkpm.productservice.service;

import g5.kttkpm.productservice.dto.CategoryDTO;
import g5.kttkpm.productservice.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ProductService {
    
    // Basic CRUD Operations
    Page<Product> getAllProducts(Pageable pageable);
    Product getProductById(String id);
    Product createProduct(Product product);
    Product updateProduct(String id, Product productDetails);
    void deleteProduct(String id);
    
    // Search Operations
    Page<Product> searchProducts(String name, String sku, String category, String brand,
                                 BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    
    // Advanced Search with flexible criteria
    Page<Product> searchProductsAdvanced(Map<String, Object> searchCriteria, Pageable pageable);
    
    // Auto-complete search
    Page<Product> autoCompleteSearch(String query, Pageable pageable);
    
    // Price Management
    Product updateProductPrice(String id, BigDecimal newPrice, Product.PriceChangeReason reason, String changedBy);
    List<Product.PriceHistory> getPriceHistory(String id);
    
    // Inventory Management
    Product updateProductInventory(String id, Integer newQuantity, Product.QuantityChangeReason reason, String changedBy);
    List<Product.QuantityHistory> getQuantityHistory(String id);
    
    // Category Management
    Product setMainCategory(String productId, String categoryId);
    Product addCategory(String productId, String categoryId);
    Product removeCategory(String productId, String categoryId);
    List<CategoryDTO> getProductCategories(String productId);
    
    // Status Management
    Product updateProductStatus(String id, Product.ProductStatus status);
    
    // Batch Operations
    List<Product> createProducts(List<Product> products);
    Page<Product> getProductsByCategory(String categoryId, Pageable pageable);
    Page<Product> getProductsByBrand(String brand, Pageable pageable);
    
    // Additional Attributes Management
    Product updateProductAttributes(String id, Map<String, Object> attributes);
    Product removeProductAttribute(String id, String key);
    Page<Product> getAllProductsByCategoryId(String categoryId, Pageable pageable);
}
