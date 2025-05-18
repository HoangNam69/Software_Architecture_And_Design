package g5.kttkpm.adminservice.clients;

import g5.kttkpm.adminservice.dtos.CartDTO;
import g5.kttkpm.adminservice.dtos.CartItemDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Component
@Slf4j
public class CartServiceClient {

    private final WebClient webClient;
    
    public CartServiceClient(WebClient.Builder webClientBuilder, @Value("${services.cart}") String cartRoot) {
        this.webClient = webClientBuilder.baseUrl(cartRoot).build();
        log.info("Cart service client initialized with baseUrl: {}", cartRoot);
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
    
    public void checkStatus() {
        webClient.get()
            .uri("/cart/cb-status")
            .retrieve()
            .bodyToMono(Void.class)
            .block();
    }
}
