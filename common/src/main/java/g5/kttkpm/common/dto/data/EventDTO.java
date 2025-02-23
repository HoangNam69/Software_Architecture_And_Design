package g5.kttkpm.common.dto.data;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Dùng để truyền dữ liệu sự kiện giữa các microservices.
 */
@Getter
@Setter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventDTO {
    String eventId;
    String eventType;
    String source;
    LocalDateTime timestamp;
    Object payload;
}
