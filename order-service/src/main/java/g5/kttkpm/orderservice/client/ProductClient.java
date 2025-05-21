package g5.kttkpm.orderservice.client;

import g5.kttkpm.orderservice.dto.BaseDTO;
import g5.kttkpm.orderservice.dto.ProductDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class ProductClient {
    private final RestTemplate restTemplate;
    private final String productServiceUrl;
    
    public ProductClient(RestTemplate restTemplate, @Value("${services.product-service.url}") String productServiceUrl) {
        this.restTemplate = restTemplate;
        this.productServiceUrl = productServiceUrl;
    }
    
    public ProductDTO getProductById(String productId) {
        log.info("Fetching product with ID: {}", productId);
        
        try {
            // Use ParameterizedTypeReference to correctly handle generic type
            ResponseEntity<BaseDTO<ProductDTO>> response = restTemplate.exchange(
                productServiceUrl + "/" + productId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
            );
            
            BaseDTO<ProductDTO> productResponse = response.getBody();
            log.debug("Product response received: {}", productResponse);
            return productResponse.data();
        } catch (Exception e) {
            log.error("Error fetching product with ID {}: {}", productId, e.getMessage());
            throw e;
        }
    }
    
    public ProductDTO updateProductInventory(String productId, Integer newQuantity, String changeReason) {
        log.info("Updating inventory for product ID: {}, new quantity: {}, reason: {}",
            productId, newQuantity, changeReason);
        
        try {
            String url = String.format("%s/%s/inventory?newQuantity=%d&reason=%s&changedBy=%s",
                productServiceUrl,
                productId,
                newQuantity,
                changeReason,
                "order-service");
            
            ResponseEntity<BaseDTO<ProductDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                null,
                new ParameterizedTypeReference<>() {
                }
            );
            
            BaseDTO<ProductDTO> productResponse = response.getBody();
            log.debug("Product inventory update response: {}", productResponse);
            return productResponse.data();
        } catch (Exception e) {
            log.error("Error updating inventory for product ID {}: {}", productId, e.getMessage());
            throw e;
        }
    }
}
