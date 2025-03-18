package g5.kttkpm.orderservice.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private String orderId;
    private String status;
    private int totalAmount;
    private String customerName;
    private String customerPhone;
}