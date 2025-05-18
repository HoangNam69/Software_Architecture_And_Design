package g5.kttkpm.categoryservice.service;

import g5.kttkpm.categoryservice.entity.Category;
import g5.kttkpm.categoryservice.payload.CategoryPayload;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public interface CategoryService {
    List<Category> getAllCategories();
    
    Category getCategoryById(UUID id);
    
    Category createCategory(CategoryPayload categoryPayload);
    
    Category updateCategory(UUID id, Category category);
    
    void deleteCategory(UUID id);
    
    // New method to delete a category and all its children
    void deleteCategoryAndChildren(UUID id);
    
    List<Category> searchCategoriesByName(String name);
    
    // Methods for subcategories
    List<Category> getSubcategories(UUID parentId);
    
    Category addSubcategory(UUID parentId, CategoryPayload categoryPayload);
    
    // Methods for metadata
    Map<String, String> getCategoryMetadata(UUID categoryId);
    
    Category updateCategoryMetadata(UUID categoryId, Map<String, String> metadata);
    
    Category addCategoryMetadataItem(UUID categoryId, String key, String value);
    
    void removeCategoryMetadataItem(UUID categoryId, String key);
}
