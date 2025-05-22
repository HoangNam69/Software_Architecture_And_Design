package g5.kttkpm.categoryservice.controller;

import g5.kttkpm.categoryservice.entity.Category;
import g5.kttkpm.categoryservice.payload.CategoryPayload;
import g5.kttkpm.categoryservice.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {
    private final CategoryService categoryService;
    
    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }
    
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }
    
    @PostMapping("/create")
    public ResponseEntity<Category> createCategory(@RequestBody CategoryPayload categoryPayload) {
        Category createdCategory = categoryService.createCategory(categoryPayload);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }
    
    @PutMapping("/{id}/edit")
    public ResponseEntity<Category> updateCategory(@PathVariable UUID id, @RequestBody Category category) {
        Category updatedCategory = categoryService.updateCategory(id, category);
        return ResponseEntity.ok(updatedCategory);
    }
    
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Category> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
    
    // New endpoint for deleting a category and all its children
    @DeleteMapping("/{id}/delete-with-children")
    public ResponseEntity<Void> deleteCategoryAndChildren(@PathVariable UUID id) {
        categoryService.deleteCategoryAndChildren(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping(params = "name")
    public ResponseEntity<List<Category>> searchCategoriesByName(@RequestParam String name) {
        List<Category> categories = categoryService.searchCategoriesByName(name);
        return ResponseEntity.ok(categories);
    }
    
    // Endpoints for subcategories
    @GetMapping("/{id}/subcategories")
    public ResponseEntity<List<Category>> getSubcategories(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.getSubcategories(id));
    }
    
    @PostMapping("/{id}/subcategories/create")
    public ResponseEntity<Category> addSubcategory(
        @PathVariable UUID id,
        @RequestBody CategoryPayload categoryPayload) {
        Category createdSubcategory = categoryService.addSubcategory(id, categoryPayload);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSubcategory);
    }
    
    // Endpoints for metadata
    @GetMapping("/{id}/metadata")
    public ResponseEntity<Map<String, String>> getCategoryMetadata(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.getCategoryMetadata(id));
    }
    
    @PutMapping("/{id}/metadata")
    public ResponseEntity<Category> updateCategoryMetadata(
        @PathVariable UUID id,
        @RequestBody Map<String, String> metadata) {
        Category updatedCategory = categoryService.updateCategoryMetadata(id, metadata);
        return ResponseEntity.ok(updatedCategory);
    }
    
    @PostMapping("/{id}/metadata")
    public ResponseEntity<Category> addCategoryMetadataItem(
        @PathVariable UUID id,
        @RequestParam String key,
        @RequestParam String value) {
        Category updatedCategory = categoryService.addCategoryMetadataItem(id, key, value);
        return ResponseEntity.ok(updatedCategory);
    }
    
    @DeleteMapping("/{id}/metadata/{key}")
    public ResponseEntity<Void> removeCategoryMetadataItem(
        @PathVariable UUID id,
        @PathVariable String key) {
        categoryService.removeCategoryMetadataItem(id, key);
        return ResponseEntity.noContent().build();
    }
}
