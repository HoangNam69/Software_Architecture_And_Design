package g5.kttkpm.categoryservice.repository;

import g5.kttkpm.categoryservice.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BrandRepository extends JpaRepository<Brand, UUID> {
    List<Brand> findAllByCategory_Id(UUID categoryId);
    
    Optional<Brand> findByNameContainsIgnoreCaseAndCategory_Id(String name, UUID categoryId);
}
