package g5.kttkpm.orderservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private String id;
    private String productId;
    private String name;
    private String sku;
    private String description;
    private String brand;
    private String thumbnailUrl;
    private List<String> imageUrls;
    private BigDecimal basePrice;
    private BigDecimal currentPrice;
    private Integer totalQuantity;
    private String mainCategoryId;
    private List<String> additionalCategoryIds;
    private String status;
    private Map<String, Object> attributes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public enum QuantityChangeReason {
        ORDER_PLACEMENT,
        ORDER_CANCELLATION,
        MANUAL_ADJUSTMENT,
        STOCK_RECEIVED,
        INVENTORY_CORRECTION,
        RETURN_TO_STOCK,
        DAMAGED_GOODS,
        RESERVED
    }
}
