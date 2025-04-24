package g5.kttkpm.orderservice.controller;

import g5.kttkpm.orderservice.dto.OrderRequest;
import g5.kttkpm.orderservice.dto.OrderResponse;
import g5.kttkpm.orderservice.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackCreateOrder")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    public ResponseEntity<OrderResponse> fallbackCreateOrder(OrderRequest request, Throwable t) {
        OrderResponse fallbackResponse = new OrderResponse();
        fallbackResponse.setOrderId(null);
        fallbackResponse.setStatus("FAILED");
        fallbackResponse.setCustomerName(request.getCustomerName());
        fallbackResponse.setCustomerPhone(request.getCustomerPhone());
        return ResponseEntity.status(503).body(fallbackResponse);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }
}