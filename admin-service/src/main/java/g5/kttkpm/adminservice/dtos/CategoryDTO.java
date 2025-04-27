package g5.kttkpm.adminservice.dtos;

import lombok.Data;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
public class CategoryDTO {
    private UUID id;
    private String name;
    private UUID parentId;
    private Set<UUID> childrenIds;
    private Map<String, String> metadata;
}