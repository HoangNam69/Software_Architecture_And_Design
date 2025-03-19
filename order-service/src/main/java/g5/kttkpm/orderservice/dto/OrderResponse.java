package g5.kttkpm.orderservice.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private Long orderId;
    private String status;
    private BigDecimal totalAmount;
    private String customerName;
    private String customerPhone;
}