package g5.kttkpm.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDTO {
    private String productId;
    private String productName;
    private int quantity;
    private int pricePerUnit;
    private int totalPrice;
}
