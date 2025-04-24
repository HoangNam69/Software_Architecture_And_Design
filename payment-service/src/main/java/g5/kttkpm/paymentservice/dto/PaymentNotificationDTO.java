package g5.kttkpm.paymentservice.dto;

import java.util.Date;

public record PaymentNotificationDTO(
    String orderCode,
    String status,
    Integer amount,
    String paymentMethod,
    String transactionId
) {}
