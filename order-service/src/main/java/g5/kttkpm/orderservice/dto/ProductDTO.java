package g5.kttkpm.orderservice.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    private String id;
    private String name;
    private int price;
}