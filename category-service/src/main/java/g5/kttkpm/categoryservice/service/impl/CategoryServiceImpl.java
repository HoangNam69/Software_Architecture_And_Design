package g5.kttkpm.categoryservice.service.impl;

import g5.kttkpm.categoryservice.entity.Category;
import g5.kttkpm.categoryservice.repository.CategoryRepository;
import g5.kttkpm.categoryservice.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category getCategoryById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category ID Not Found: " + id));
    }

    @Override
    public Category createCategory(Category category) {
        Optional<Category> existingCategory = categoryRepository.findByName(category.getName());

        if (existingCategory.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category with name '" + category.getName() + "' already exists.");
        }

        return categoryRepository.save(category);
    }

    @Override
    public Category updateCategory(UUID id, Category category) {
        if (!categoryRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category ID Not Found");
        }

        category.setId(id);
        return categoryRepository.save(category);
    }

    @Override
    public void deleteCategory(UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category ID Not Found: " + id);
        }
        categoryRepository.deleteById(id);
    }

    @Override
    public List<Category> searchCategoriesByName(String name) {
        List<Category> categories = categoryRepository.findByNameContainingIgnoreCase(name);
        if (categories.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No categories found with name: " + name);
        }
        return categories;
    }
}
