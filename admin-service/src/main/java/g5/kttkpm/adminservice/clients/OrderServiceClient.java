package g5.kttkpm.adminservice.clients;

import g5.kttkpm.adminservice.dtos.OrderDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class OrderServiceClient {
    
    private final WebClient webClient;
    
    public OrderServiceClient(WebClient.Builder webClientBuilder, @Value("${services.order}") String orderRoot) {
        this.webClient = webClientBuilder.baseUrl(orderRoot).build();
        log.info("Order service client initialized with baseUrl: {}", orderRoot);
    }
    
    // Create Order
    public Mono<OrderDTO> createOrder(OrderDTO orderDTO) {
        return webClient.post()
            .uri("")
            .bodyValue(orderDTO)
            .retrieve()
            .bodyToMono(OrderDTO.class);
    }
    
    // Get all orders
    public Mono<OrderDTO[]> getAllOrders() {
        return webClient.get()
            .uri("")
            .retrieve()
            .bodyToMono(OrderDTO[].class);
    }
    
    // Get order by ID
    public Mono<OrderDTO> getOrderById(Long id) {
        return webClient.get()
            .uri("/{id}", id)
            .retrieve()
            .bodyToMono(OrderDTO.class);
    }
    
    // Update Order
    public Mono<OrderDTO> updateOrder(Long id, OrderDTO orderDTO) {
        return webClient.put()
            .uri("/{id}", id)
            .bodyValue(orderDTO)
            .retrieve()
            .bodyToMono(OrderDTO.class);
    }
    
    // Delete Order
    public Mono<Void> deleteOrder(Long id) {
        return webClient.delete()
            .uri("/{id}", id)
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
