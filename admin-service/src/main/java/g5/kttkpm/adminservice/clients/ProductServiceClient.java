package g5.kttkpm.adminservice.clients;

import g5.kttkpm.adminservice.dtos.ProductDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ProductServiceClient {

    private final WebClient webClient;

    public ProductServiceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8083/api/v1/products").build();
    }

    // Lấy tất cả sản phẩm
    public Mono<ProductDTO[]> getAllProducts() {
        return webClient.get()
                .uri("")
                .retrieve()
                .bodyToMono(ProductDTO[].class);
    }

    // Lấy sản phẩm theo ID
    public Mono<ProductDTO> getProductById(String id) {
        return webClient.get()
                .uri("/{id}", id)
                .retrieve()
                .bodyToMono(ProductDTO.class);
    }

    // Tạo sản phẩm mới
    public Mono<ProductDTO> createProduct(ProductDTO product) {
        return webClient.post()
                .uri("")
                .bodyValue(product)
                .retrieve()
                .bodyToMono(ProductDTO.class);
    }

    // Cập nhật sản phẩm
    public Mono<ProductDTO> updateProduct(String id, ProductDTO product) {
        return webClient.put()
                .uri("/{id}", id)
                .bodyValue(product)
                .retrieve()
                .bodyToMono(ProductDTO.class);
    }

    // Xoá sản phẩm
    public Mono<Void> deleteProduct(String id) {
        return webClient.delete()
                .uri("/{id}", id)
                .retrieve()
                .bodyToMono(Void.class);
    }
}