package g5.kttkpm.productservice.repo;

import g5.kttkpm.productservice.model.ProductImage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends MongoRepository<ProductImage, String> {
    Optional<ProductImage> findByProductId(String productId);
}