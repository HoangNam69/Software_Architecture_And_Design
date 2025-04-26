package g5.kttkpm.productservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ListResponse<T>(
    List<T> content,
    int page,
    int size,
    @JsonProperty("total_elements")
    long totalElements,
    @JsonProperty("total_pages")
    int totalPages,
    boolean first,
    boolean last
) {
}
