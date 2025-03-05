package g5.kttkpm.categoryservice.controller;

import g5.kttkpm.categoryservice.entity.Brand;
import g5.kttkpm.categoryservice.payload.BrandPayload;
import g5.kttkpm.categoryservice.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories/brands")
public class BrandController {
    
    private final BrandService brandService;
    
    @Autowired
    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }
    
    @GetMapping
    public ResponseEntity<List<Brand>> getAllBrandByCategoryId(@RequestParam("category_id") UUID categoryId) {
        return ResponseEntity.ok(brandService.getAllBrandsByCategory(categoryId));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Brand> getBrandById(@PathVariable UUID id) {
        Optional<Brand> brandOpt = brandService.getBrandById(id);
        
        if (brandOpt.isPresent()) {
            return ResponseEntity.ok(brandOpt.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/create")
    public ResponseEntity<Brand> createBrand(@RequestBody BrandPayload payload) {
        Brand newBrand = brandService.createBrand(payload);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(newBrand);
    }
    
    @PutMapping("/{id}/edit")
    public ResponseEntity<Brand> updateBrand(@PathVariable UUID id, @RequestBody Brand brand) {
        Brand updatedBrand = brandService.updateBrand(id, brand);
        return ResponseEntity.ok(updatedBrand);
    }
    
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> deleteBrand(@PathVariable UUID id) {
        brandService.deleteBrand(id);
        return ResponseEntity.noContent().build();
    }
}
