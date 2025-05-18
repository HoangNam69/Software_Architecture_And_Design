package g5.kttkpm.categoryservice.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "categories")
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "id")
public class Category {
    
    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id")
    @JsonBackReference
    private Category parent;
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private Set<Category> children = new HashSet<>();
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "category_metadata",
        joinColumns = @JoinColumn(name = "category_id"))
    @MapKeyColumn(name = "key")
    @Column(name = "value")
    private Map<String, String> metadata = new HashMap<>();
    
    public Category() {
    }
    
    public Category(UUID id, String name) {
        this.id = id;
        this.name = name;
    }
    
    // Helper method to add a child category
    public void addChild(Category child) {
        children.add(child);
        child.setParent(this);
    }
    
    // Helper method to remove a child category
    public void removeChild(Category child) {
        children.remove(child);
        child.setParent(null);
    }
    
    // Helper methods for metadata
    public void addMetadata(String key, String value) {
        metadata.put(key, value);
    }
    
    public String getMetadataValue(String key) {
        return metadata.get(key);
    }
    
    public void removeMetadata(String key) {
        metadata.remove(key);
    }
    
    // Helper method to check if this category has children
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }
}
