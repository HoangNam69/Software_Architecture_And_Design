package g5.kttkpm.productservice.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class CategoryDTO {
    private String id;  // or UUID with proper conversion
    private String name;
    private List<CategoryDTO> children;
    private Map<String, Object> metadata;
}
