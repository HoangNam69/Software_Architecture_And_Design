package g5.kttkpm.adminservice.clients;

import g5.kttkpm.adminservice.dtos.CartDTO;
import g5.kttkpm.adminservice.dtos.CartItemDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Component
public class CartServiceClient {

    private final WebClient webClient;

    public CartServiceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8086/api/v1").build();
    }

    // Get cart by userId
    public Mono<CartDTO> getCart(String userId) {
        return webClient.get()
                .uri("/cart/{userId}", userId)
                .retrieve()
                .bodyToMono(CartDTO.class);
    }

    // Add item to cart
    public Mono<Void> addToCart(String userId, CartItemDTO cartItemDTO) {
        return webClient.post()
                .uri("/cart/{userId}", userId)
                .bodyValue(cartItemDTO)
                .retrieve()
                .bodyToMono(Void.class);
    }

    // Update item quantity in cart
    public Mono<Void> updateCartItemQuantity(String userId, String productId, int quantity) {
        return webClient.put()
                .uri("/{userId}/cart/{productId}?quantity={quantity}", userId, productId, quantity)
                .retrieve()
                .bodyToMono(Void.class);
    }

    // Remove item from cart
    public Mono<Void> removeFromCart(String userId, String productId) {
        return webClient.delete()
                .uri("/{userId}/cart/{productId}", userId, productId)
                .retrieve()
                .bodyToMono(Void.class);
    }

    // Clear entire cart
    public Mono<Void> clearCart(String userId) {
        return webClient.delete()
                .uri("/cart/{userId}", userId)
                .retrieve()
                .bodyToMono(Void.class);
    }

    // Get total cart value
    public Mono<BigDecimal> getCartTotal(String userId) {
        return webClient.get()
                .uri("/total/{userId}", userId)
                .retrieve()
                .bodyToMono(BigDecimal.class);
    }
}