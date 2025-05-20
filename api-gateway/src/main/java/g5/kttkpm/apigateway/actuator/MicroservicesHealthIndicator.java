package g5.kttkpm.apigateway.actuator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
public class MicroservicesHealthIndicator implements HealthIndicator {
    
    private final WebClient webClient;
    
    // Map của các service cần kiểm tra - Tên của service và URL health check
    private final Map<String, String> serviceHealthEndpoints;
    
    // Inject timeout configuration
    @Value("${app.health.timeout-seconds:3}")
    private int timeoutSeconds;
    
    // Inject service URLs from configuration
    @Value("${app.services.admin-service:http://admin-service:8087}")
    private String adminServiceUrl;
    
    @Value("${app.services.authentication-service:http://authentication-service:8081}")
    private String authenticationServiceUrl;
    
    @Value("${app.services.cart-service:http://cart-service:8086}")
    private String cartServiceUrl;
    
    @Value("${app.services.category-service:http://category-service:8082}")
    private String categoryServiceUrl;
    
    @Value("${app.services.order-service:http://order-service:8085}")
    private String orderServiceUrl;
    
    @Value("${app.services.payment-service:http://payment-service:8084}")
    private String paymentServiceUrl;
    
    @Value("${app.services.product-service:http://product-service:8083}")
    private String productServiceUrl;
    
    @Value("${app.services.report-service:http://report-service:8088}")
    private String reportServiceUrl;
    
    // Path for health actuator endpoint - can be customized
    @Value("${app.health.endpoint-path:/actuator/health}")
    private String healthEndpointPath;
    
    public MicroservicesHealthIndicator(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
        this.serviceHealthEndpoints = new HashMap<>();
    }
    
    // Initialize the service map after all properties are set
    private void initServiceMap() {
        if (serviceHealthEndpoints.isEmpty()) {
            serviceHealthEndpoints.put("admin-service", adminServiceUrl + healthEndpointPath);
            serviceHealthEndpoints.put("authentication-service", authenticationServiceUrl + healthEndpointPath);
            serviceHealthEndpoints.put("cart-service", cartServiceUrl + healthEndpointPath);
            serviceHealthEndpoints.put("category-service", categoryServiceUrl + healthEndpointPath);
            serviceHealthEndpoints.put("order-service", orderServiceUrl + healthEndpointPath);
            serviceHealthEndpoints.put("payment-service", paymentServiceUrl + healthEndpointPath);
            serviceHealthEndpoints.put("product-service", productServiceUrl + healthEndpointPath);
            serviceHealthEndpoints.put("report-service", reportServiceUrl + healthEndpointPath);
        }
    }
    
    @Override
    public Health health() {
        initServiceMap(); // Initialize the map before using it
        
        Map<String, Object> details = new HashMap<>();
        boolean allServicesUp = true;
        
        for (Map.Entry<String, String> entry : serviceHealthEndpoints.entrySet()) {
            String serviceName = entry.getKey();
            String healthEndpoint = entry.getValue();
            
            try {
                // Gửi request kiểm tra health và đợi kết quả
                // Sử dụng timeout từ cấu hình
                String response = webClient.get()
                    .uri(healthEndpoint)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .onErrorResume(e -> {
                        if (e instanceof WebClientResponseException) {
                            return Mono.just("Status: " + ((WebClientResponseException) e).getStatusCode());
                        }
                        return Mono.just("Error: " + e.getMessage());
                    })
                    .block();
                
                if (response != null && response.contains("OK")) {
                    details.put(serviceName, "UP");
                } else {
                    details.put(serviceName, response != null ? response : "DOWN");
                    allServicesUp = false;
                }
            } catch (Exception e) {
                details.put(serviceName, "DOWN - " + e.getMessage());
                allServicesUp = false;
            }
        }
        
        return allServicesUp
            ? Health.up().withDetails(details).build()
            : Health.down().withDetails(details).build();
    }
}
