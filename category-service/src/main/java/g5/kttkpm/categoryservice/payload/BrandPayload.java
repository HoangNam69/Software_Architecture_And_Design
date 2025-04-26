package g5.kttkpm.categoryservice.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record BrandPayload(
    String name,
    @JsonProperty("category_id")
    UUID categoryId
) {
}
