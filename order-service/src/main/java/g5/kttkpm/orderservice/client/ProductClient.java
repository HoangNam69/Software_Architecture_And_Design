package g5.kttkpm.orderservice.client;

import g5.kttkpm.orderservice.dto.ProductDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ProductClient {
    private final RestTemplate restTemplate;
    private final String productServiceUrl;

    public ProductClient(RestTemplate restTemplate, @Value("${services.product-service.url}") String productServiceUrl) {
        this.restTemplate = restTemplate;
        this.productServiceUrl = productServiceUrl;
    }

    public ProductDTO getProductById(String productId) {
        return restTemplate.getForObject(productServiceUrl + "/" + productId, ProductDTO.class);
    }
}