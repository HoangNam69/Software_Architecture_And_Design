package g5.kttkpm.paymentservice.dto;

public record PaymentResponseDTO(
    String paymentUrl,
    String orderCode,
    String status
) {}
