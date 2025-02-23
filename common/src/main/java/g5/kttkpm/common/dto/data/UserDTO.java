package g5.kttkpm.common.dto.data;

import g5.kttkpm.common.dto.BaseDTO;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * Dùng để truyền dữ liệu người dùng giữa các services.
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDTO extends BaseDTO {
    String username;
    String email;
    String fullName;
    String role;
}
