package g5.kttkpm.common.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class BaseDTO implements Serializable {
    Long id;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
