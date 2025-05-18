package g5.kttkpm.adminservice.controllers;

import g5.kttkpm.adminservice.clients.*;
import g5.kttkpm.adminservice.responses.CircuitBreakerResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/cb-check")
@AllArgsConstructor
@Slf4j
public class CircuitBreakerController {
    
    private static final String STATUS_OFFLINE = "OFFLINE";
    private static final String STATUS_ONLINE = "ONLINE";
    
    private static final String SERVICE_AUTH = "Auth Service";
    private static final String SERVICE_CART = "Cart Service";
    private static final String SERVICE_CATEGORY = "Category Service";
    private static final String SERVICE_ORDER = "Order Service";
    private static final String SERVICE_PAYMENT = "Payment Service";
    private static final String SERVICE_PRODUCT = "Product Service";
    
    private final AuthServiceClient authClient;
    private final CartServiceClient cartClient;
    private final CategoryServiceClient categoryClient;
    private final OrderServiceClient orderClient;
    private final PaymentServiceClient paymentClient;
    private final ProductServiceClient productClient;
    
    /**
     * Get status of all services at once
     */
    @GetMapping("/all")
    public ResponseEntity<List<CircuitBreakerResponse>> checkAllServicesStatus() {
        List<CircuitBreakerResponse> responses = new ArrayList<>();
        
        // Auth service
        try {
            responses.add(checkAuthServiceStatus().getBody());
        } catch (Exception e) {
            log.warn("Auth service down.", e);
            responses.add(genResponse(SERVICE_AUTH, false));
        }
        
        // Cart service
        try {
            responses.add(checkCartServiceStatus().getBody());
        } catch (Exception e) {
            log.warn("Cart service down.", e);
            responses.add(genResponse(SERVICE_CART, false));
        }
        
        // Category service
        try {
            responses.add(checkCategoryServiceStatus().getBody());
        } catch (Exception e) {
            log.warn("Category service down.", e);
            responses.add(genResponse(SERVICE_CATEGORY, false));
        }
        
        // Order service
        try {
            responses.add(checkOrderServiceStatus().getBody());
        } catch (Exception e) {
            log.warn("Order service down.", e);
            responses.add(genResponse(SERVICE_ORDER, false));
        }
        
        // Payment service
        try {
            responses.add(checkPaymentServiceStatus().getBody());
        } catch (Exception e) {
            log.warn("Payment service down.", e);
            responses.add(genResponse(SERVICE_PAYMENT, false));
        }
        
        // Product service
        try {
            responses.add(checkProductServiceStatus().getBody());
        } catch (Exception e) {
            log.warn("Product service down.", e);
            responses.add(genResponse(SERVICE_PRODUCT, false));
        }
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Get status of a specific service by name
     */
    @GetMapping("/{serviceName}")
    public ResponseEntity<CircuitBreakerResponse> checkServiceByName(@PathVariable String serviceName) {
        return switch (serviceName.toLowerCase()) {
            case "auth" -> checkAuthServiceStatus();
            case "cart" -> checkCartServiceStatus();
            case "category" -> checkCategoryServiceStatus();
            case "order" -> checkOrderServiceStatus();
            case "payment" -> checkPaymentServiceStatus();
            case "product" -> checkProductServiceStatus();
            default -> ResponseEntity.badRequest().body(
                new CircuitBreakerResponse(serviceName, "UNKNOWN", false)
            );
        };
    }
    
    @GetMapping("/auth")
    @CircuitBreaker(
        name = SERVICE_AUTH,
        fallbackMethod = "fallbackStatusAuthService"
    )
    public ResponseEntity<CircuitBreakerResponse> checkAuthServiceStatus() {
        authClient.checkStatus();
        return ResponseEntity.ok(genResponse(SERVICE_AUTH, true));
    }
    
    @GetMapping("/cart")
    @CircuitBreaker(
        name = SERVICE_CART,
        fallbackMethod = "fallbackStatusCartService"
    )
    public ResponseEntity<CircuitBreakerResponse> checkCartServiceStatus() {
        cartClient.checkStatus();
        return ResponseEntity.ok(genResponse(SERVICE_CART, true));
    }
    
    @GetMapping("/category")
    @CircuitBreaker(
        name = SERVICE_CATEGORY,
        fallbackMethod = "fallbackStatusCategoryService"
    )
    public ResponseEntity<CircuitBreakerResponse> checkCategoryServiceStatus() {
        categoryClient.checkStatus();
        return ResponseEntity.ok(genResponse(SERVICE_CATEGORY, true));
    }
    
    @GetMapping("/order")
    @CircuitBreaker(
        name = SERVICE_ORDER,
        fallbackMethod = "fallbackStatusOrderService"
    )
    public ResponseEntity<CircuitBreakerResponse> checkOrderServiceStatus() {
        orderClient.checkStatus();
        return ResponseEntity.ok(genResponse(SERVICE_ORDER, true));
    }
    
    @GetMapping("/payment")
    @CircuitBreaker(
        name = SERVICE_PAYMENT,
        fallbackMethod = "fallbackStatusPaymentService"
    )
    public ResponseEntity<CircuitBreakerResponse> checkPaymentServiceStatus() {
        paymentClient.checkStatus();
        return ResponseEntity.ok(genResponse(SERVICE_PAYMENT, true));
    }
    
    @GetMapping("/product")
    @CircuitBreaker(
        name = SERVICE_PRODUCT,
        fallbackMethod = "fallbackStatusProductService"
    )
    public ResponseEntity<CircuitBreakerResponse> checkProductServiceStatus() {
        productClient.checkStatus();
        return ResponseEntity.ok(genResponse(SERVICE_PRODUCT, true));
    }
    
    public ResponseEntity<CircuitBreakerResponse> fallbackStatusAuthService(Exception e) {
        log.warn("Auth service down.", e);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
            genResponse(SERVICE_AUTH, false)
        );
    }
    
    public ResponseEntity<CircuitBreakerResponse> fallbackStatusCartService(Exception e) {
        log.warn("Cart service down.", e);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
            genResponse(SERVICE_CART, false)
        );
    }
    
    public ResponseEntity<CircuitBreakerResponse> fallbackStatusCategoryService(Exception e) {
        log.warn("Category service down.", e);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
            genResponse(SERVICE_CATEGORY, false)
        );
    }
    
    public ResponseEntity<CircuitBreakerResponse> fallbackStatusOrderService(Exception e) {
        log.warn("Order service down.", e);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
            genResponse(SERVICE_ORDER, false)
        );
    }
    
    public ResponseEntity<CircuitBreakerResponse> fallbackStatusPaymentService(Exception e) {
        log.warn("Payment service down.", e);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
            genResponse(SERVICE_PAYMENT, false)
        );
    }
    
    public ResponseEntity<CircuitBreakerResponse> fallbackStatusProductService(Exception e) {
        log.warn("Product service down.", e);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
            genResponse(SERVICE_PRODUCT, false)
        );
    }
    
    private CircuitBreakerResponse genResponse(String serviceName, boolean isOnline) {
        if (isOnline)
            return new CircuitBreakerResponse(serviceName, STATUS_ONLINE, isOnline);
        else
            return new CircuitBreakerResponse(serviceName, STATUS_OFFLINE, isOnline);
    }
}
