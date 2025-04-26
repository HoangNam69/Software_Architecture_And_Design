package g5.kttkpm.orderservice.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDTO {
    private String productId;
    private int quantity;
}