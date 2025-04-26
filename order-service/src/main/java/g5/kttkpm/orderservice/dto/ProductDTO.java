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
    private BigDecimal currentPrice; // Đảm bảo sử dụng BigDecimal
    
    // Để quản lý số lượng sản phẩm trên hệ thống
    private Integer totalQuantity;
    private Integer availableQuantity;
    private Integer reservedQuantity;
}
