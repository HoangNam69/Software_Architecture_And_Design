package g5.kttkpm.adminservice.clients;

import g5.kttkpm.adminservice.dtos.PaymentDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class PaymentServiceClient {

    private final WebClient webClient;

    public PaymentServiceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8084/api/v1/payments").build();
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
}