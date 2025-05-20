package g5.kttkpm.adminservice.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemDTO {
    private String productId;
    private String productName;
    private int quantity;
    private BigDecimal price;
    private String productThumbnail;
}