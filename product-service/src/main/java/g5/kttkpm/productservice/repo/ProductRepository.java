package g5.kttkpm.productservice.repo;

import g5.kttkpm.productservice.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    Page<Product> findByBrandIgnoreCase(String brand, Pageable pageable);
    
    Optional<Product> findByProductId(String productId);
    
    Page<Product> findByMainCategoryIdOrAdditionalCategoriesContaining(String mainCategoryId, String additionalCategory, Pageable pageable);
}
