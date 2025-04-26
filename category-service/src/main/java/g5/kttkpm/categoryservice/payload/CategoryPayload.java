package g5.kttkpm.categoryservice.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.UUID;

public record CategoryPayload(
    String name,
    @JsonProperty("parent_id")
    UUID parentId,
    Map<String, String> metadata
) {
}
