package g5.kttkpm.orderservice.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    private String id;
    private String name;
    private String description;
    
    private BigDecimal basePrice;
    private BigDecimal currentPrice;
    private BigDecimal costPrice;
    
    // Để quản lý số lượng sản phẩm trên hệ thống
    private Integer totalQuantity;
    private Integer availableQuantity;
    private Integer reservedQuantity;
}
