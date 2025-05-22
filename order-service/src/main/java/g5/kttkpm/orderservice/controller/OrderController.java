package g5.kttkpm.orderservice.controller;

import g5.kttkpm.orderservice.client.PaymentClient;
import g5.kttkpm.orderservice.dto.*;
import g5.kttkpm.orderservice.entity.Order;
import g5.kttkpm.orderservice.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    private final PaymentClient paymentClient;
    
    @Value("${services.callback.success}")
    private String paymentCallbackSuccessUrl;
    
    @Value("${services.callback.cancel}")
    private String paymentCallbackCancelUrl;
    
    @PostMapping
    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackCreateOrder")
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }
    
    public ResponseEntity<OrderResponse> fallbackCreateOrder(OrderRequest request, Throwable t) {
        OrderResponse fallbackResponse = new OrderResponse();
        fallbackResponse.setStatus("FAILED");
        fallbackResponse.setCustomerName(request.getCustomerName());
        fallbackResponse.setCustomerPhone(request.getCustomerPhone());
        return ResponseEntity.status(503).body(fallbackResponse);
    }
    
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders(
        @RequestParam(name = "user_id", required = false) String userId
    ) {
        if (userId != null)
            return ResponseEntity.ok(orderService.getOrderByUserId(userId));
        
        return ResponseEntity.ok(orderService.getAllOrders());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @RequestBody Order updatedOrder) {
        Order existingOrder = orderService.getOrderById(id);
        if (existingOrder == null) {
            return ResponseEntity.notFound().build();
        }

        existingOrder.setStatus(updatedOrder.getStatus());
        existingOrder.setCustomerName(updatedOrder.getCustomerName());
        existingOrder.setCustomerEmail(updatedOrder.getCustomerEmail());
        existingOrder.setCustomerPhone(updatedOrder.getCustomerPhone());
        existingOrder.setCustomerAddress(updatedOrder.getCustomerAddress());

        orderService.updateOrder(existingOrder);
        return ResponseEntity.ok(existingOrder);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        Order existingOrder = orderService.getOrderById(id);
        if (existingOrder == null) {
            return ResponseEntity.notFound().build();
        }
        orderService.deleteOrderById(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * API để tạo đơn hàng và chuyển đến thanh toán
     */
    @PostMapping("/checkout")
    public ResponseEntity<?> createOrderAndPay(@RequestBody OrderRequest orderRequest) {
        try {
            // 1. Tạo đơn hàng mới
            Order order = orderService.createOrder(orderRequest);
            
            
            // 2. Chuẩn bị dữ liệu thanh toán
            PaymentRequestDTO paymentRequest = new PaymentRequestDTO();
            paymentRequest.setAmount(order.getTotalAmount().intValue());
            paymentRequest.setDescription("TTDH #" + order.getId());
            
            // Chuyển đổi danh sách sản phẩm
            paymentRequest.setProducts(order.getItems().stream()
                .map(item -> new PaymentRequestDTO.ProductData(
                    item.getProductName(),
                    item.getQuantity(),
                    item.getPricePerUnit().intValue()
                ))
                .collect(Collectors.toList()));
            
            // Cấu hình URL callback
            paymentRequest.setReturnUrl(paymentCallbackSuccessUrl + order.getId());
            paymentRequest.setCancelUrl(paymentCallbackCancelUrl + order.getId());
            
            // Thông tin người mua (nếu có)
            paymentRequest.setBuyerName(order.getCustomerName());
            paymentRequest.setBuyerEmail(order.getCustomerEmail());
            paymentRequest.setBuyerPhone(order.getCustomerPhone());
            
            // 3. Gọi đến payment-service để tạo liên kết thanh toán
            PaymentResponseDTO paymentResponse = paymentClient.getPaymentLink(paymentRequest);
            
            // 4. Cập nhật thông tin đơn hàng với kết quả từ payment-service
            if (paymentResponse != null) {
                
                // Cập nhật đơn hàng với mã thanh toán và URL
                order.setPaymentOrderCode(paymentResponse.orderCode());
                order.setPaymentUrl(paymentResponse.paymentUrl());
                order.setStatus("AWAITING_PAYMENT");
                
                orderService.updatePaymentUrlAndStatusByPaymentOrderCode(order);
                
                // Trả về URL thanh toán cho frontend
                return ResponseEntity.ok(paymentResponse);
            } else {
                // Xử lý lỗi từ payment-service
                order.setPaymentOrderCode("");
                order.setPaymentUrl("");
                order.setStatus("PAYMENT_FAILED");
                orderService.updatePaymentUrlAndStatusByPaymentOrderCode(order);
                
                return ResponseEntity.badRequest().body("Không thể tạo liên kết thanh toán");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi khi xử lý đơn hàng: " + e.getMessage());
        }
    }
    
    /**
     * API callback khi thanh toán thành công (từ frontend)
     */
    @GetMapping("/payment/success")
    public ResponseEntity<String> paymentSuccess(@RequestParam Long orderId) {
        Order order = orderService.getOrderById(orderId);
        if (order != null) {
            // Frontend đã quay lại, chờ webhook từ payment-service để cập nhật chính thức
            return ResponseEntity.ok("Đang xử lý thanh toán, vui lòng chờ xác nhận...");
        }
        return ResponseEntity.badRequest().body("Không tìm thấy đơn hàng");
    }
    
    /**
     * API callback khi thanh toán bị hủy (từ frontend)
     */
    @GetMapping("/payment/cancel")
    public ResponseEntity<String> paymentCancel(@RequestParam Long orderId) {
        Order order = orderService.getOrderById(orderId);
        if (order != null) {
            // Cập nhật trạng thái đơn hàng
            order.setStatus("CANCELLED");
            orderService.updatePaymentUrlAndStatusByPaymentOrderCode(order);
            
            return ResponseEntity.ok("Đơn hàng đã bị hủy");
        }
        return ResponseEntity.badRequest().body("Không tìm thấy đơn hàng");
    }
    
    /**
     * API nhận thông báo từ payment-service (webhook internal)
     */
    @PostMapping("/payment/notification")
    public ResponseEntity<?> handlePaymentNotification(@RequestBody PaymentNotificationDTO notification) {
        // Tìm đơn hàng theo mã thanh toán
        Order order = orderService.findByPaymentOrderCode(notification.orderCode());
        
        if (order != null) {
            // Cập nhật trạng thái đơn hàng dựa trên thông báo
            switch (notification.status()) {
                case "SUCCESS":
                    order.setStatus("PAID");
                    order.setPaymentMethod(notification.paymentMethod());
                    order.setPaymentTransactionId(notification.transactionId());
                    break;
                
                case "CANCELLED":
                    order.setStatus("CANCELLED");
                    break;
                
                case "FAILED":
                    order.setStatus("PAYMENT_FAILED");
                    break;
                
                default:
                    order.setStatus("PAYMENT_PENDING");
                    break;
            }
            
            // Lưu thông tin cập nhật
            orderService.updateOrder(order);
            
            return ResponseEntity.ok().build();
        }
        
        return ResponseEntity.badRequest().body("Không tìm thấy đơn hàng với mã thanh toán: " + notification.orderCode());
    }
}
