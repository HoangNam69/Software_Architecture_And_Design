package g5.kttkpm.adminservice.clients;

import g5.kttkpm.adminservice.dtos.PaymentDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class PaymentServiceClient {
    
    private final WebClient webClient;
    
    public PaymentServiceClient(WebClient.Builder webClientBuilder, @Value("${services.payment}") String paymentRoot) {
        this.webClient = webClientBuilder.baseUrl(paymentRoot).build();
        log.info("Payment service client initialized with baseUrl: {}", paymentRoot);
    }
    
    // Create payment
    public Mono<PaymentDTO> createPayment(PaymentDTO paymentDTO) {
        return webClient.post()
            .uri("/create")
            .bodyValue(paymentDTO)
            .retrieve()
            .bodyToMono(PaymentDTO.class);
    }
    
    // Get payment by id
    public Mono<PaymentDTO> getPaymentById(Long id) {
        return webClient.get()
            .uri("/{id}", id)
            .retrieve()
            .bodyToMono(PaymentDTO.class);
    }
    
    // Update payment
    public Mono<PaymentDTO> updatePayment(Long id, PaymentDTO paymentDTO) {
        return webClient.put()
            .uri("/{id}", id)
            .bodyValue(paymentDTO)
            .retrieve()
            .bodyToMono(PaymentDTO.class);
    }
    
    // Delete payment
    public Mono<Void> deletePayment(Long id) {
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
