package g5.kttkpm.adminservice.clients;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import g5.kttkpm.adminservice.dtos.OrderDTO;
import reactor.core.publisher.Mono;
@Component
public class OrderServiceClient {

    private final WebClient webClient;

    public OrderServiceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8085/api/v1/orders").build();
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
}