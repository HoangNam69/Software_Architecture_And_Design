package g5.kttkpm.categoryservice.service;

import g5.kttkpm.categoryservice.entity.Brand;
import g5.kttkpm.categoryservice.payload.BrandPayload;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BrandService {
    List<Brand> getAllBrandsByCategory(UUID categoryId);
    
    Optional<Brand> getBrandById(UUID brandId);
    
    Brand createBrand(BrandPayload newBrandPayload);
    
    Brand updateBrand(UUID id, Brand updateBrand);
    
    void deleteBrand(UUID brandId);
}
