package g5.kttkpm.orderservice.client;

import g5.kttkpm.orderservice.dto.PaymentRequestDTO;
import g5.kttkpm.orderservice.dto.PaymentResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PaymentClient {
    private final RestTemplate restTemplate;
    private final String paymentServiceUrl;
    
    public PaymentClient(RestTemplate restTemplate, @Value("${services.payment-service.url}") String paymentServiceUrl) {
        this.restTemplate = restTemplate;
        this.paymentServiceUrl = paymentServiceUrl;
    }
    
    public ResponseEntity<PaymentResponseDTO> getPaymentLink(PaymentRequestDTO paymentRequest) {
        return restTemplate.postForEntity(paymentServiceUrl + "/create", paymentRequest, PaymentResponseDTO.class);
    }
}
