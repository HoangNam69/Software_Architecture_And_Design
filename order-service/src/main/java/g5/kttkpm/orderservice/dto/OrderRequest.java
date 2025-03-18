package g5.kttkpm.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String customerAddress;
    private List<OrderItemDTO> items;
}
