package g5.kttkpm.productservice.controller;

import g5.kttkpm.productservice.dto.CategoryDTO;
import g5.kttkpm.productservice.dto.ListResponse;
import g5.kttkpm.productservice.model.Product;
import g5.kttkpm.productservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

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
    public ResponseEntity<ListResponse<Product>> getAllProducts(
        @RequestParam(name = "category_id", required = false) String categoryId,
        @RequestParam(name = "page", defaultValue = "1") int page,
        @RequestParam(name = "size", defaultValue = "20") int size,
        @RequestParam(name = "sort_by", defaultValue = "currentPrice") String sortBy,
        @RequestParam(name = "sort_dir", defaultValue = "desc") String sortDir
    ) {
        // Create a Pageable object with the given page, size, sortBy, and sortDir
        Pageable pageable = createPageable(page, size, sortBy, sortDir);

        // If get by category
        if (categoryId != null) {
            Page<Product> products = productService.getAllProductsByCategoryId(categoryId, pageable);
            ListResponse<Product> listResponse =createListResponse(products);
            return ResponseEntity.ok(listResponse);
        }

        Page<Product> products = productService.getAllProducts(pageable);
        ListResponse<Product> listResponse = createListResponse(products);
        return ResponseEntity.ok(listResponse);
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
    @RateLimiter(name = "productSearchLimiter", fallbackMethod = "fallbackSearch")
    public ResponseEntity<ListResponse<Product>> searchProducts(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String sku,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String brand,
        @RequestParam(name = "page", defaultValue = "1") int page,
        @RequestParam(name = "size", defaultValue = "20") int size,
        @RequestParam(name = "sort_by", defaultValue = "name") String sortBy,
        @RequestParam(name = "sort_dir", defaultValue = "asc") String sortDir) {

        // Create a Pageable object with the given page, size, sortBy, and sortDir
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        Page<Product> products = productService.searchProducts(name, sku, category, brand, pageable);
        ListResponse<Product> listResponse = createListResponse(products);

        return ResponseEntity.ok(
            listResponse
        );
    }

    public ResponseEntity<ListResponse<Product>> fallbackSearch(
        String name, String sku, String category, String brand,
        int page, int size, String sortBy, String sortDir, Throwable t) {
        return ResponseEntity.status(429).body(
            new ListResponse<>(
                java.util.Collections.emptyList(), // content
                1,     // page
                20,    // size
                0L,    // total elements
                0,     // total pages
                true,  // isFirst
                true   // isLast
            )
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
    public ResponseEntity<ListResponse<Product>> getProductsByCategory(
        @PathVariable String categoryId,  // Changed from UUID to String
        @RequestParam(name = "page", defaultValue = "1") int page,
        @RequestParam(name = "size", defaultValue = "20") int size,
        @RequestParam(name = "sort_by", defaultValue = "name") String sortBy,
        @RequestParam(name = "sort_dir", defaultValue = "asc") String sortDir) {
        
        // Create a Pageable object with the given page, size, sortBy, and sortDir
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        Page<Product> products = productService.getProductsByCategory(categoryId, pageable);
        ListResponse<Product> listResponse = createListResponse(products);
        return ResponseEntity.ok(listResponse);
    }
    
    @GetMapping("/brand/{brand}")
    public ResponseEntity<ListResponse<Product>> getProductsByBrand(
        @PathVariable String brand,
        @RequestParam(name = "page", defaultValue = "1") int page,
        @RequestParam(name = "size", defaultValue = "20") int size,
        @RequestParam(name = "sort_by", defaultValue = "name") String sortBy,
        @RequestParam(name = "sort_dir", defaultValue = "asc") String sortDir) {
        
        // Create a Pageable object with the given page, size, sortBy, and sortDir
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        Page<Product> products = productService.getProductsByBrand(brand, pageable);
        ListResponse<Product> listResponse = createListResponse(products);
        
        return ResponseEntity.ok(listResponse);
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
    
    private Pageable createPageable(int page, int size, String sortBy, String sortDir) {
        return PageRequest.
            of(Math.max(page - 1, 0),
                size > 0 ? size : 20,
                sortDir.equals("asc") ? org.springframework.data.domain.Sort.by(sortBy).ascending() : org.springframework.data.domain.Sort.by(sortBy).descending());
    }
    
    private ListResponse<Product> createListResponse(Page<Product> products) {
        return new ListResponse<>(
            products.getContent(),
            products.getNumber() + 1,
            products.getSize(),
            products.getTotalElements(),
            products.getTotalPages(),
            products.isFirst(),
            products.isLast());
    }
}
