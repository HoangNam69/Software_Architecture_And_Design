package g5.kttkpm.productservice.client;

import g5.kttkpm.productservice.dto.CategoryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class CategoryClient {
    private final RestTemplate restTemplate;
    private final String categoryServiceUrl;
    
    @Autowired
    public CategoryClient(RestTemplate restTemplate,
                          @Value("${services.category-service.url}") String categoryServiceUrl) {
        this.restTemplate = restTemplate;
        this.categoryServiceUrl = categoryServiceUrl;
    }
    
    public CategoryDTO getCategoryById(String id) {
        return restTemplate.getForObject(categoryServiceUrl + "/" + id, CategoryDTO.class);
    }
    
    public List<CategoryDTO> getAllCategories() {
        ResponseEntity<List<CategoryDTO>> response = restTemplate.exchange(
            categoryServiceUrl,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {
            }
        );
        return response.getBody();
    }
}
