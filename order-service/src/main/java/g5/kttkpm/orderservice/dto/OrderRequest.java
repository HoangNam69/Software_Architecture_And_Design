package g5.kttkpm.orderservice.dto;

import lombok.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    private String userId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String customerAddress;
    private List<OrderItemDTO> items;
}
