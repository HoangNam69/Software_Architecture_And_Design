package g5.kttkpm.adminservice.clients;

import g5.kttkpm.adminservice.dtos.CategoryDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Slf4j
public class CategoryServiceClient {
    
    private final WebClient webClient;
    
    public CategoryServiceClient(WebClient.Builder webClientBuilder, @Value("${services.category}") String categoryRoot) {
        this.webClient = webClientBuilder.baseUrl(categoryRoot).build();
        log.info("Category service client initialized with baseUrl: {}", categoryRoot);
    }
    
    // Get all categories
    public Flux<CategoryDTO> getAllCategories() {
        return webClient.get()
            .uri("")
            .retrieve()
            .bodyToFlux(CategoryDTO.class);
    }
    
    // Get category by ID
    public Mono<CategoryDTO> getCategoryById(UUID id) {
        return webClient.get()
            .uri("/{id}", id)
            .retrieve()
            .bodyToMono(CategoryDTO.class);
    }
    
    // Create a new category
    public Mono<CategoryDTO> createCategory(CategoryDTO categoryDTO) {
        return webClient.post()
            .uri("/create")
            .bodyValue(categoryDTO)
            .retrieve()
            .bodyToMono(CategoryDTO.class);
    }
    
    // Update a category
    public Mono<CategoryDTO> updateCategory(UUID id, CategoryDTO categoryDTO) {
        return webClient.put()
            .uri("/{id}/edit", id)
            .bodyValue(categoryDTO)
            .retrieve()
            .bodyToMono(CategoryDTO.class);
    }
    
    // Delete a category
    public Mono<Void> deleteCategory(UUID id) {
        return webClient.delete()
            .uri("/{id}/delete", id)
            .retrieve()
            .bodyToMono(Void.class);
    }
    
    public void checkStatus() {
        webClient.get()
            .uri("/cb-status")
            .retrieve()
            .bodyToMono(Void.class)
            .block();
    }
}
