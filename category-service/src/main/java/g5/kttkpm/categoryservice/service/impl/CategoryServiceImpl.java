package g5.kttkpm.categoryservice.service.impl;

import g5.kttkpm.categoryservice.entity.Category;
import g5.kttkpm.categoryservice.payload.CategoryPayload;
import g5.kttkpm.categoryservice.repository.CategoryRepository;
import g5.kttkpm.categoryservice.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    
    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    
    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAllParentCategories();
    }
    
    @Override
    public Category getCategoryById(UUID id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category ID Not Found: " + id));
    }
    
    @Override
    public Category createCategory(CategoryPayload categoryPayload) {
        Optional<Category> existingCategory = categoryRepository.findByName(categoryPayload.name());
        
        if (existingCategory.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category with name '" + categoryPayload.name() + "' already exists.");
        }
        
        Category newCategory = new Category();
        newCategory.setName(categoryPayload.name());
        
        // Handle parent category if specified
        if (categoryPayload.parentId() != null) {
            Category parentCategory = getCategoryById(categoryPayload.parentId());
            newCategory.setParent(parentCategory);
        }
        
        // Handle metadata if provided
        if (categoryPayload.metadata() != null) {
            newCategory.setMetadata(categoryPayload.metadata());
        }
        
        return categoryRepository.save(newCategory);
    }
    
    @Override
    public Category updateCategory(UUID id, Category category) {
        Category existingCategory = getCategoryById(id);
        
        existingCategory.setName(category.getName());
        
        // Don't override children and parent relationships
        
        // Only update metadata if it's not null in the incoming category
        if (category.getMetadata() != null && !category.getMetadata().isEmpty()) {
            existingCategory.setMetadata(category.getMetadata());
        }
        
        return categoryRepository.save(existingCategory);
    }
    
    @Override
    public void deleteCategory(UUID id) {
        Category category = getCategoryById(id);
        
        // Check if the category has children
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Cannot delete category with children. Delete children first or reassign them.");
        }
        
        categoryRepository.deleteById(id);
    }
    
    @Override
    public List<Category> searchCategoriesByName(String name) {
        List<Category> categoriesV2 = categoryRepository.findByNameContainingIgnoreCase(name);
        if (categoriesV2.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No categories found with name: " + name);
        }
        return categoriesV2;
    }
    
    @Override
    public List<Category> getSubcategories(UUID parentId) {
        Category parentCategory = getCategoryById(parentId);
        return new ArrayList<>(parentCategory.getChildren());
    }
    
    @Override
    public Category addSubcategory(UUID parentId, CategoryPayload categoryPayload) {
        // First check if parent exists
        Category parentCategory = getCategoryById(parentId);
        
        // Create new subcategory
        CategoryPayload updatedPayload = new CategoryPayload(
            categoryPayload.name(),
            parentId,
            categoryPayload.metadata()
        );
        
        // Use existing create method
        Category newSubcategory = createCategory(updatedPayload);
        
        // Update parent's children list
        parentCategory.addChild(newSubcategory);
        categoryRepository.save(parentCategory);
        
        return newSubcategory;
    }
    
    @Override
    public Map<String, String> getCategoryMetadata(UUID categoryId) {
        Category category = getCategoryById(categoryId);
        return category.getMetadata();
    }
    
    @Override
    public Category updateCategoryMetadata(UUID categoryId, Map<String, String> metadata) {
        Category category = getCategoryById(categoryId);
        category.setMetadata(metadata);
        return categoryRepository.save(category);
    }
    
    @Override
    public Category addCategoryMetadataItem(UUID categoryId, String key, String value) {
        Category category = getCategoryById(categoryId);
        category.addMetadata(key, value);
        return categoryRepository.save(category);
    }
    
    @Override
    public void removeCategoryMetadataItem(UUID categoryId, String key) {
        Category category = getCategoryById(categoryId);
        category.removeMetadata(key);
        categoryRepository.save(category);
    }
}
