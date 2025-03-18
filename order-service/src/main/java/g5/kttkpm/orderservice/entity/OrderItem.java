package g5.kttkpm.orderservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {
    private String productId;
    private String productName;
    private int quantity;
    private int pricePerUnit;
    private int totalPrice;
}