package g5.kttkpm.orderservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class PaymentRequestDTO {
    
    private Integer amount;
    private String description;
    private List<ProductData> products;
    private String cancelUrl;
    private String returnUrl;
    private String buyerName;
    private String buyerEmail;
    private String buyerPhone;
    
    @Data
    public static class ProductData {
        private String name;
        private Integer quantity;
        private Integer price;
        
        public ProductData(String name, Integer quantity, Integer price) {
            this.name = name;
            this.quantity = quantity;
            this.price = price;
        }
    }
}
