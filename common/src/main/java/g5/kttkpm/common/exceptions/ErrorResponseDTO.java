package g5.kttkpm.common.exceptions;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Chuẩn hóa phản hồi lỗi API.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ErrorResponseDTO {
    int status;
    String message;
    String errorCode;
    LocalDateTime timestamp;
}
