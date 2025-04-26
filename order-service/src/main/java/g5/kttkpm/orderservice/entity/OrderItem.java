package g5.kttkpm.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    private String productId;
    
    @Column(name = "product_name")
    private String productName;
    
    private int quantity;
    
    @Column(name = "price_per_unit")
    private BigDecimal pricePerUnit;
    
    @Column(name = "total_price")
    private BigDecimal totalPrice;
}
