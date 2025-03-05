package g5.kttkpm.productservice.controller;

import g5.kttkpm.productservice.dto.CategoryDTO;
import g5.kttkpm.productservice.model.Product;
import g5.kttkpm.productservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    private final ProductService productService;
    
    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    
    // Basic CRUD Operations
    
    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(Pageable pageable) {
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }
    
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product createdProduct = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
        @PathVariable String id,
        @RequestBody Product product) {
        Product updatedProduct = productService.updateProduct(id, product);
        return ResponseEntity.ok(updatedProduct);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
    
    // Search Operations
    
    @GetMapping("/search")
    public ResponseEntity<Page<Product>> searchProducts(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String sku,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String brand,
        Pageable pageable) {
        return ResponseEntity.ok(
            productService.searchProducts(name, sku, category, brand, pageable)
        );
    }
    
    // Price Management
    
    @PutMapping("/{id}/price")
    public ResponseEntity<Product> updateProductPrice(
        @PathVariable String id,
        @RequestParam BigDecimal newPrice,
        @RequestParam(required = false) Product.PriceChangeReason reason,
        @RequestParam(required = false, defaultValue = "system") String changedBy) {
        
        Product.PriceChangeReason changeReason = (reason != null) ?
            reason : Product.PriceChangeReason.MANUAL_UPDATE;
        
        Product updatedProduct = productService.updateProductPrice(id, newPrice, changeReason, changedBy);
        return ResponseEntity.ok(updatedProduct);
    }
    
    @GetMapping("/{id}/price-history")
    public ResponseEntity<List<Product.PriceHistory>> getPriceHistory(@PathVariable String id) {
        return ResponseEntity.ok(productService.getPriceHistory(id));
    }
    
    // Inventory Management
    
    @PutMapping("/{id}/inventory")
    public ResponseEntity<Product> updateProductInventory(
        @PathVariable String id,
        @RequestParam Integer newQuantity,
        @RequestParam(required = false) Product.QuantityChangeReason reason,
        @RequestParam(required = false, defaultValue = "system") String changedBy) {
        
        Product.QuantityChangeReason changeReason = (reason != null) ?
            reason : Product.QuantityChangeReason.MANUAL_ADJUSTMENT;
        
        Product updatedProduct = productService.updateProductInventory(
            id, newQuantity, changeReason, changedBy);
        return ResponseEntity.ok(updatedProduct);
    }
    
    @GetMapping("/{id}/quantity-history")
    public ResponseEntity<List<Product.QuantityHistory>> getQuantityHistory(@PathVariable String id) {
        return ResponseEntity.ok(productService.getQuantityHistory(id));
    }
    
    // Category Management
    
    @PutMapping("/{id}/main-category/{categoryId}")
    public ResponseEntity<Product> setMainCategory(
        @PathVariable String id,
        @PathVariable String categoryId) {  // Changed from UUID to String
        Product product = productService.setMainCategory(id, categoryId);
        return ResponseEntity.ok(product);
    }
    
    @PostMapping("/{id}/categories/{categoryId}")
    public ResponseEntity<Product> addCategory(
        @PathVariable String id,
        @PathVariable String categoryId) {  // Changed from UUID to String
        Product product = productService.addCategory(id, categoryId);
        return ResponseEntity.ok(product);
    }
    
    @DeleteMapping("/{id}/categories/{categoryId}")
    public ResponseEntity<Product> removeCategory(
        @PathVariable String id,
        @PathVariable String categoryId) {  // Changed from UUID to String
        Product product = productService.removeCategory(id, categoryId);
        return ResponseEntity.ok(product);
    }
    
    @GetMapping("/{id}/categories")
    public ResponseEntity<List<CategoryDTO>> getProductCategories(
        @PathVariable String id) {
        List<CategoryDTO> categories = productService.getProductCategories(id);
        return ResponseEntity.ok(categories);
    }
    
    // Status Management
    
    @PutMapping("/{id}/status")
    public ResponseEntity<Product> updateProductStatus(
        @PathVariable String id,
        @RequestParam Product.ProductStatus status) {
        Product updatedProduct = productService.updateProductStatus(id, status);
        return ResponseEntity.ok(updatedProduct);
    }
    
    // Batch Operations
    
    @PostMapping("/batch")
    public ResponseEntity<List<Product>> createProducts(@RequestBody List<Product> products) {
        List<Product> createdProducts = productService.createProducts(products);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProducts);
    }
    
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<Product>> getProductsByCategory(
        @PathVariable String categoryId,  // Changed from UUID to String
        Pageable pageable) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId, pageable));
    }
    
    @GetMapping("/brand/{brand}")
    public ResponseEntity<Page<Product>> getProductsByBrand(
        @PathVariable String brand,
        Pageable pageable) {
        return ResponseEntity.ok(productService.getProductsByBrand(brand, pageable));
    }
    
    // Additional Attributes Management
    
    @PutMapping("/{id}/attributes")
    public ResponseEntity<Product> updateProductAttributes(
        @PathVariable String id,
        @RequestBody java.util.Map<String, Object> attributes) {
        Product updatedProduct = productService.updateProductAttributes(id, attributes);
        return ResponseEntity.ok(updatedProduct);
    }
    
    @DeleteMapping("/{id}/attributes/{key}")
    public ResponseEntity<Product> removeProductAttribute(
        @PathVariable String id,
        @PathVariable String key) {
        Product updatedProduct = productService.removeProductAttribute(id, key);
        return ResponseEntity.ok(updatedProduct);
    }
}
