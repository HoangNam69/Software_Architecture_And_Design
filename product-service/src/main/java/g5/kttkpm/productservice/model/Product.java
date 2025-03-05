package g5.kttkpm.productservice.model;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class Product {
    @MongoId
    private String id;
    
    // Basic product information
    private String name;
    private String sku;
    private String description;
    private String brand;
    
    // Main Category
    private String mainCategoryId;
    private String mainCategoryName;
    
    // Additional categories
    private List<CategoryReference> additionalCategories = new ArrayList<>();
    
    // Pricing details
    private BigDecimal basePrice;
    private BigDecimal currentPrice;
    private BigDecimal costPrice;
    
    // Inventory management
    private Integer totalQuantity;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    
    // Price history tracking
    private List<PriceHistory> priceHistory;
    
    // Quantity history tracking
    private List<QuantityHistory> quantityHistory;
    
    // Flexible attributes for future extensibility
    private Map<String, Object> additionalAttributes;
    
    // Product status and metadata
    private ProductStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Nested class for price history
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceHistory {
        private BigDecimal price;
        private LocalDateTime changedAt;
        private String changedBy;
        private PriceChangeReason reason;
    }
    
    // Nested class for quantity history
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuantityHistory {
        private Integer quantity;
        private LocalDateTime changedAt;
        private String changedBy;
        private QuantityChangeReason reason;
    }
    
    // Nested class to store category references
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryReference {
        private String categoryId;
        private String categoryName;
    }
    
    // Enums for tracking changes
    public enum ProductStatus {
        ACTIVE,
        INACTIVE,
        OUT_OF_STOCK,
        DISCONTINUED
    }
    
    public enum PriceChangeReason {
        INITIAL_PRICING,
        SEASONAL_SALE,
        COMPETITOR_PRICING,
        COST_ADJUSTMENT,
        MANUAL_UPDATE
    }
    
    public enum QuantityChangeReason {
        INITIAL_STOCK,
        SALE,
        RESTOCK,
        RETURN,
        MANUAL_ADJUSTMENT,
        DAMAGED_STOCK
    }
    
    // Utility methods
    public void updatePrice(BigDecimal newPrice, PriceChangeReason reason, String changedBy) {
        // Initialize price history if null
        if (this.priceHistory == null) {
            this.priceHistory = new ArrayList<>();
        }
        
        // Add current price to price history before updating
        PriceHistory priceChangeRecord = PriceHistory.builder()
            .price(this.currentPrice)
            .changedAt(LocalDateTime.now())
            .changedBy(changedBy)
            .reason(reason)
            .build();
        
        this.priceHistory.add(priceChangeRecord);
        this.currentPrice = newPrice;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void updateQuantity(Integer newQuantity, QuantityChangeReason reason, String changedBy) {
        // Initialize quantity history if null
        if (this.quantityHistory == null) {
            this.quantityHistory = new ArrayList<>();
        }
        
        // Add current quantity to quantity history before updating
        QuantityHistory quantityChangeRecord = QuantityHistory.builder()
            .quantity(this.totalQuantity)
            .changedAt(LocalDateTime.now())
            .changedBy(changedBy)
            .reason(reason)
            .build();
        
        this.quantityHistory.add(quantityChangeRecord);
        this.totalQuantity = newQuantity;
        this.updatedAt = LocalDateTime.now();
    }
    
    // Utility method to add a category
    public void addCategory(String categoryId, String categoryName) {
        // Initialize additionalCategories if null
        if (this.additionalCategories == null) {
            this.additionalCategories = new ArrayList<>();
        }
        
        CategoryReference categoryRef = new CategoryReference(categoryId, categoryName);
        if (!containsCategory(categoryId)) {
            additionalCategories.add(categoryRef);
        }
    }
    
    // Utility method to check if a category exists
    public boolean containsCategory(String categoryId) {
        if (mainCategoryId != null && mainCategoryId.equals(categoryId)) {
            return true;
        }
        
        if (additionalCategories == null) {
            return false;
        }
        
        return additionalCategories.stream()
            .anyMatch(cat -> cat.getCategoryId().equals(categoryId));
    }
    
    // Utility method to set main category
    public void setMainCategory(String categoryId, String categoryName) {
        // Initialize additionalCategories if null
        if (this.additionalCategories == null) {
            this.additionalCategories = new ArrayList<>();
        }
        
        // If it was in additional categories, remove it first
        additionalCategories.removeIf(cat -> cat.getCategoryId().equals(categoryId));
        
        // Set as main category
        this.mainCategoryId = categoryId;
        this.mainCategoryName = categoryName;
    }
    
    // Utility method to remove a category
    public void removeCategory(String categoryId) {
        // If it's the main category, clear it
        if (mainCategoryId != null && mainCategoryId.equals(categoryId)) {
            mainCategoryId = null;
            mainCategoryName = null;
        } else {
            // Otherwise remove from additional categories
            additionalCategories.removeIf(cat -> cat.getCategoryId().equals(categoryId));
        }
    }
}
