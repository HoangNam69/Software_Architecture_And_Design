package g5.kttkpm.categoryservice.service.impl;

import g5.kttkpm.categoryservice.entity.Brand;
import g5.kttkpm.categoryservice.entity.Category;
import g5.kttkpm.categoryservice.payload.BrandPayload;
import g5.kttkpm.categoryservice.repository.BrandRepository;
import g5.kttkpm.categoryservice.repository.CategoryRepository;
import g5.kttkpm.categoryservice.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BrandServiceImpl implements BrandService {
    
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    
    @Autowired
    public BrandServiceImpl(BrandRepository brandRepository, CategoryRepository categoryRepository) {
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
    }
    
    @Override
    public List<Brand> getAllBrandsByCategory(UUID categoryId) {
        return brandRepository.findAllByCategory_Id(categoryId);
    }
    
    @Override
    public Optional<Brand> getBrandById(UUID brandId) {
        return brandRepository.findById(brandId);
    }
    
    @Override
    public Brand createBrand(BrandPayload newBrandPayload) {
        Optional<Brand> brandOtp = brandRepository.findByNameContainsIgnoreCaseAndCategory_Id(newBrandPayload.name(), newBrandPayload.categoryId());
        if (brandOtp.isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already has this Brand name in that Category");
        
        Optional<Category> categoryOtp= categoryRepository.findById(newBrandPayload.categoryId());
        
        if (categoryOtp.isEmpty())
            throw  new ResponseStatusException(HttpStatus.NOT_FOUND, "Category with id'" + newBrandPayload.categoryId() + "' not found.");
        
        Brand newBrand = new Brand();
        newBrand.setName(newBrandPayload.name());
        newBrand.setCategory(categoryOtp.get());
        return brandRepository.save(newBrand);
    }
    
    @Override
    public Brand updateBrand(UUID id, Brand updateBrand) {
        Optional<Brand> existingBrandOtp = getBrandById(id);
        if (existingBrandOtp.isEmpty())
            throw  new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand with id'" + id.toString() + "' not found.");
        
        Brand existingBrand = existingBrandOtp.get();
        existingBrand.setName(updateBrand.getName());
        
        return brandRepository.save(existingBrand);
    }
    
    @Override
    public void deleteBrand(UUID brandId) {
        brandRepository.deleteById(brandId);
    }
}
