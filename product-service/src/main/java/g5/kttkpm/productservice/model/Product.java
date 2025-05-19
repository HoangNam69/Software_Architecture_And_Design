package g5.kttkpm.productservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class Product {
    @Id
    private String id;
    private String productId;
    
    // Basic product information
    private String name;
    private String sku;
    private String description;
    private String brand;
    @Builder.Default
    private String thumbnailUrl = "https://architecture-system-design.s3.ap-southeast-2.amazonaws.com/defaults/default-product-image.png";
    private List<String> imageUrls; // Added to match frontend model
    
    // Main Category
    private String mainCategoryId;
    
    // Additional categories
    private List<String> additionalCategories = new ArrayList<>();
    
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
    
    // Nested class for price history - updated to match frontend
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceHistory {
        private BigDecimal oldPrice; // Changed from price to oldPrice
        private BigDecimal newPrice; // Added newPrice field
        private LocalDateTime timestamp; // Renamed from changedAt to timestamp
        private String changedBy;
        private PriceChangeReason changeReason; // Renamed from reason to changeReason
    }
    
    // Nested class for quantity history - updated to match frontend
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuantityHistory {
        private Integer oldQuantity; // Changed from quantity to oldQuantity
        private Integer newQuantity; // Added newQuantity field
        private LocalDateTime timestamp; // Renamed from changedAt to timestamp
        private String changedBy;
        private QuantityChangeReason changeReason; // Renamed from reason to changeReason
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
    
    // Utility methods - updated to match new fields
    public void updatePrice(BigDecimal newPrice, PriceChangeReason reason, String changedBy) {
        // Initialize price history if null
        if (this.priceHistory == null) {
            this.priceHistory = new ArrayList<>();
        }
        
        // Add current price to price history before updating
        PriceHistory priceChangeRecord = PriceHistory.builder()
            .oldPrice(this.currentPrice)
            .newPrice(newPrice)
            .timestamp(LocalDateTime.now())
            .changedBy(changedBy)
            .changeReason(reason)
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
            .oldQuantity(this.totalQuantity)
            .newQuantity(newQuantity)
            .timestamp(LocalDateTime.now())
            .changedBy(changedBy)
            .changeReason(reason)
            .build();
        
        this.quantityHistory.add(quantityChangeRecord);
        this.totalQuantity = newQuantity;
        this.updatedAt = LocalDateTime.now();
    }
    
    // Utility method to add a category
    public void addCategory(String categoryId) {
        // Initialize additionalCategories if null
        if (this.additionalCategories == null) {
            this.additionalCategories = new ArrayList<>();
        }
        
        if (!containsCategory(categoryId)) {
            additionalCategories.add(categoryId);
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
            .anyMatch(cat -> cat.equals(categoryId));
    }
    
    // Utility method to set main category
    public void setMainCategory(String categoryId) {
        // Initialize additionalCategories if null
        if (this.additionalCategories == null) {
            this.additionalCategories = new ArrayList<>();
        }
        
        // If it was in additional categories, remove it first
        additionalCategories.removeIf(cat -> cat.equals(categoryId));
        
        // Set as main category
        this.mainCategoryId = categoryId;
    }
    
    // Utility method to remove a category
    public void removeCategory(String categoryId) {
        // If it's the main category, clear it
        if (mainCategoryId != null && mainCategoryId.equals(categoryId)) {
            mainCategoryId = null;
        } else {
            // Otherwise remove from additional categories
            additionalCategories.removeIf(cat -> cat.equals(categoryId));
        }
    }
}
