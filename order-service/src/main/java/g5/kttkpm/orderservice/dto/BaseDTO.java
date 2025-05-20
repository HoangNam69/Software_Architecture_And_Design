package g5.kttkpm.orderservice.dto;

public record BaseDTO<T>(Integer code, String message, T data) {
}
