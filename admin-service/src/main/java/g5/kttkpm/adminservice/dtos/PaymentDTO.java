package g5.kttkpm.adminservice.dtos;

import lombok.Data;

import java.util.Date;

@Data
public class PaymentDTO {
    private Long id;
    private String orderCode;
    private Integer amount;
    private String status;
    private String paymentUrl;
    private String paymentMethod;
    private String transactionId;
    private String description;
    private Date createdAt;
    private Date updatedAt;
}