package g5.kttkpm.common.exceptions;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Chuẩn hóa phản hồi lỗi API.
 */
@Getter
public class ErrorResponseDTO {
    private final int status;
    private final String message;
    private final String errorCode;
    private final LocalDateTime timestamp;
    
    public ErrorResponseDTO(ErrorCodes errorCode, String customMessage) {
        this.status = errorCode.getHttpStatus();
        this.message = customMessage != null ? customMessage : errorCode.getMessage();
        this.errorCode = errorCode.getCode();
        this.timestamp = LocalDateTime.now();
    }
}
