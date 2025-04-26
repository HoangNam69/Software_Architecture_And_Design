package g5.kttkpm.orderservice.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {
    private String id;
    private String status;
    private BigDecimal totalAmount;
    private String customerName;
    private String customerPhone;
}
