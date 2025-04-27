package g5.kttkpm.adminservice.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDTO {
    private String id;
    private String name;
    private String sku;
    private String description;
    private String brand;
    private String thumbnailUrl;
    private String mainCategoryId;
    private List<String> additionalCategories;
    private BigDecimal basePrice;
    private BigDecimal currentPrice;
    private BigDecimal costPrice;
    private Integer totalQuantity;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private ProductStatus status;

    public enum ProductStatus {
        ACTIVE,
        INACTIVE,
        OUT_OF_STOCK,
        DISCONTINUED
    }
}