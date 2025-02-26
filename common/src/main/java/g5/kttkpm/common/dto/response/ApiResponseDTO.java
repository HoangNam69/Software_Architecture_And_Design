package g5.kttkpm.common.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Chuẩn hóa phản hồi API.
 *
 * @param <T> Class data tương ứng
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiResponseDTO<T> {
    private boolean success;
    private String message;
    private T data;
    
    public ApiResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
