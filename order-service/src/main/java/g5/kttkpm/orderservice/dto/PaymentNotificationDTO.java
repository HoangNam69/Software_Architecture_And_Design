package g5.kttkpm.orderservice.dto;

import java.util.Date;

public record PaymentNotificationDTO(
    String orderCode,
    String status,
    Integer amount,
    String paymentMethod,
    String transactionId,
    Date updatedAt
) {}
