package g5.kttkpm.productservice.service;

import g5.kttkpm.productservice.dto.CategoryDTO;
import g5.kttkpm.productservice.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ProductService {
    
    // Basic CRUD operations
    Page<Product> getAllProducts(Pageable pageable);
    Product getProductById(String id);
    Product createProduct(Product product);
    Product updateProduct(String id, Product product);
    void deleteProduct(String id);
    
    // Search operations
    Page<Product> searchProducts(String name, String sku, String category, String brand, Pageable pageable);
    
    // Price management
    Product updateProductPrice(String id, BigDecimal newPrice, Product.PriceChangeReason reason, String changedBy);
    List<Product.PriceHistory> getPriceHistory(String id);
    
    // Inventory management
    Product updateProductInventory(String id, Integer newQuantity, Product.QuantityChangeReason reason, String changedBy);
    List<Product.QuantityHistory> getQuantityHistory(String id);
    
    // Category management
    Product setMainCategory(String productId, String categoryId);
    Product addCategory(String productId, String categoryId);
    Product removeCategory(String productId, String categoryId);
    List<CategoryDTO> getProductCategories(String productId);
    
    // Status management
    Product updateProductStatus(String id, Product.ProductStatus status);
    
    // Batch operations
    List<Product> createProducts(List<Product> products);
    Page<Product> getProductsByCategory(String categoryId, Pageable pageable); // Changed from UUID to String
    Page<Product> getProductsByBrand(String brand, Pageable pageable);
    
    // Additional attributes management
    Product updateProductAttributes(String id, Map<String, Object> attributes);
    Product removeProductAttribute(String id, String key);
}
