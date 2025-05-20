package g5.kttkpm.apigateway.actuator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ApiGatewayInfoContributor implements InfoContributor {
        
        @Value("${spring.application.name:api-gateway}")
        private String applicationName;
        
        @Value("${app.gateway.description:API Gateway for Microservices Architecture}")
        private String description;
        
        @Value("${app.gateway.version:1.0}")
        private String version;
        
        // Service descriptions
        @Value("${app.services.description.admin-service:Service for administration tasks}")
        private String adminServiceDesc;
        
        @Value("${app.services.description.authentication-service:Service for user authentication and authorization}")
        private String authServiceDesc;
        
        @Value("${app.services.description.cart-service:Service for managing shopping cart}")
        private String cartServiceDesc;
        
        @Value("${app.services.description.category-service:Service for managing product categories}")
        private String categoryServiceDesc;
        
        @Value("${app.services.description.order-service:Service for managing orders}")
        private String orderServiceDesc;
        
        @Value("${app.services.description.payment-service:Service for processing payments}")
        private String paymentServiceDesc;
        
        @Value("${app.services.description.product-service:Service for managing products}")
        private String productServiceDesc;
        
        @Value("${app.services.description.report-service:Service for generating reports}")
        private String reportServiceDesc;
        
        @Override
        public void contribute(Info.Builder builder) {
                Map<String, Object> details = new HashMap<>();
                details.put("name", applicationName);
                details.put("description", description);
                details.put("version", version);
                
                Map<String, Object> serviceDetails = new HashMap<>();
                serviceDetails.put("admin-service", adminServiceDesc);
                serviceDetails.put("authentication-service", authServiceDesc);
                serviceDetails.put("cart-service", cartServiceDesc);
                serviceDetails.put("category-service", categoryServiceDesc);
                serviceDetails.put("order-service", orderServiceDesc);
                serviceDetails.put("payment-service", paymentServiceDesc);
                serviceDetails.put("product-service", productServiceDesc);
                serviceDetails.put("report-service", reportServiceDesc);
                
                details.put("services", serviceDetails);
                
                builder.withDetail("gateway", details);
        }
}
