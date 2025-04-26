package g5.kttkpm.orderservice.dto;

public record PaymentResponseDTO(
    String paymentUrl,
    String orderCode,
    String status
) {}
