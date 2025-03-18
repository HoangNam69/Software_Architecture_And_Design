package g5.kttkpm.orderservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    @Id
    private String id;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String customerAddress;
    private int totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private List<OrderItem> items;
}
