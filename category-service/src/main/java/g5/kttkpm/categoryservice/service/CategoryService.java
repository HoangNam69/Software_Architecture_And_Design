package g5.kttkpm.categoryservice.service;

import g5.kttkpm.categoryservice.entity.Category;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public interface CategoryService {
    List<Category> getAllCategories();

    Category getCategoryById(UUID id);

    Category createCategory(Category category);

    Category updateCategory(UUID id, Category category);

    void deleteCategory(UUID id);

    List<Category> searchCategoriesByName(String name);

}
