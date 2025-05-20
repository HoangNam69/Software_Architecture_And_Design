package g5.kttkpm.orderservice.client;

import g5.kttkpm.orderservice.dto.BaseDTO;
import g5.kttkpm.orderservice.dto.PaymentRequestDTO;
import g5.kttkpm.orderservice.dto.PaymentResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class PaymentClient {
    private final RestTemplate restTemplate;
    private final String paymentServiceUrl;
    
    public PaymentClient(RestTemplate restTemplate, @Value("${services.payment-service.url}") String paymentServiceUrl) {
        this.restTemplate = restTemplate;
        this.paymentServiceUrl = paymentServiceUrl;
    }
    
    public PaymentResponseDTO getPaymentLink(PaymentRequestDTO paymentRequest) {
        log.info("Sending payment request to payment service: {}", paymentRequest);
        
        try {
            // Create HttpEntity with the request body
            HttpEntity<PaymentRequestDTO> requestEntity = new HttpEntity<>(paymentRequest);
            
            // Use exchange with proper ParameterizedTypeReference
            ResponseEntity<BaseDTO<PaymentResponseDTO>> response = restTemplate.exchange(
                paymentServiceUrl + "/create",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<BaseDTO<PaymentResponseDTO>>() {}
            );
            
            log.debug("Payment response received: {}", response.getBody());
            
            BaseDTO<PaymentResponseDTO> payment = response.getBody();
            return payment.data();
        } catch (Exception e) {
            log.error("Error getting payment link: {}", e.getMessage());
            throw e;
        }
    }
}
