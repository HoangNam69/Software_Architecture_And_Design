package g5.kttkpm.paymentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import g5.kttkpm.paymentservice.constant.PaymentMethodConstant;
import g5.kttkpm.paymentservice.constant.PaymentStatusConstant;
import g5.kttkpm.paymentservice.dto.OrderData;
import g5.kttkpm.paymentservice.dto.PaymentResponseDTO;
import g5.kttkpm.paymentservice.entity.Payment;
import g5.kttkpm.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;
import vn.payos.type.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    
    private final PayOS payOS;
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;
    
    /**
     * Tạo liên kết thanh toán từ OrderData
     */
    @PostMapping("/create")
    public ResponseEntity<?> createPaymentLink(@Valid @RequestBody OrderData orderData) {
        try {
            // Tạo mã đơn hàng
            String currentTimeString = String.valueOf(new Date().getTime());
            long orderCode = Long.parseLong(currentTimeString.substring(currentTimeString.length() - 6));
            
            // Xử lý dữ liệu sản phẩm từ OrderData
            List<ItemData> items = orderData.getProducts().stream()
                .map(p -> ItemData.builder()
                    .name(p.name())
                    .quantity(p.quantity())
                    .price(p.price())
                    .build()).toList();
            
            // Tạo dữ liệu thanh toán cho PayOS
            PaymentData.PaymentDataBuilder paymentDataBuilder = PaymentData.builder()
                .orderCode(orderCode)
                .amount(orderData.getAmount())
                .description(orderData.getDescription())
                .returnUrl(orderData.getReturnUrl())
                .cancelUrl(orderData.getCancelUrl())
                .items(items);
            
            if (orderData.getBuyerName() != null && !orderData.getBuyerName().isEmpty())
                paymentDataBuilder.buyerName(orderData.getBuyerName());
            if (orderData.getBuyerEmail() != null && !orderData.getBuyerEmail().isEmpty())
                paymentDataBuilder.buyerEmail(orderData.getBuyerEmail());
            if (orderData.getBuyerPhone() != null && !orderData.getBuyerPhone().isEmpty())
                paymentDataBuilder.buyerPhone(orderData.getBuyerPhone());
            
            PaymentData paymentData = paymentDataBuilder.build();
            
            // Gọi API PayOS để tạo liên kết thanh toán
            CheckoutResponseData response = payOS.createPaymentLink(paymentData);
            
            // Lưu thông tin thanh toán ban đầu
            Payment payment = new Payment();
            payment.setOrderCode(String.valueOf(orderCode));
            payment.setAmount(orderData.getAmount());
            payment.setStatus(PaymentStatusConstant.PENDING);
            payment.setPaymentUrl(response.getCheckoutUrl());
            payment.setDescription(orderData.getDescription());
            payment.setCreatedAt(new Date());
            paymentService.savePayment(payment);
            
            // Trả về kết quả
            return ResponseEntity.ok(new PaymentResponseDTO(
                response.getCheckoutUrl(),
                String.valueOf(orderCode),
                PaymentStatusConstant.PENDING
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to process payment.");
        }
    }
    
    /**
     * Webhook handler từ PayOS
     */
    @PostMapping("/webhook")
    public ResponseEntity<ObjectNode> handleWebhook(@RequestBody ObjectNode body) {
        ObjectNode response = objectMapper.createObjectNode();
        
        try {
            // Xác thực webhook
            Webhook webhookBody = objectMapper.treeToValue(body, Webhook.class);
            WebhookData webhookData = payOS.verifyPaymentWebhookData(webhookBody);
            
            // Cập nhật thông tin thanh toán
            String orderCode = String.valueOf(webhookData.getOrderCode());
            Payment payment = paymentService.findByOrderCode(orderCode);
            
            if (payment != null) {
                // Cập nhật trạng thái thanh toán
                if (webhookBody.getSuccess())
                    payment.setStatus(PaymentStatusConstant.SUCCESS);
                else
                    payment.setStatus(PaymentStatusConstant.CANCEL);
                payment.setPaymentMethod(PaymentMethodConstant.BANK);
                payment.setTransactionId(webhookData.getPaymentLinkId());
                paymentService.savePayment(payment);
                
                // TODO: Gửi thông báo đến order-service
                paymentService.notifyOrderService(payment);
            }
            
            // Trả về kết quả
            response.put("success", true);
            response.put("message", "Webhook delivered");
            response.set("data", null);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            response.set("data", null);
            return ResponseEntity.ok(response);
        }
    }
    
    @PostMapping(path = "/confirm-webhook")
    public ObjectNode confirmWebhook(@RequestBody Map<String, String> requestBody) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode response = objectMapper.createObjectNode();
        try {
            String str = payOS.confirmWebhook(requestBody.get("webhookUrl"));
            response.set("data", objectMapper.valueToTree(str));
            response.put("error", 0);
            response.put("message", "ok");
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", -1);
            response.put("message", e.getMessage());
            response.set("data", null);
            return response;
        }
    }
    
    /**
     * Endpoint xử lý khi thanh toán thành công
     */
    @GetMapping("/success")
    public ResponseEntity<String> paymentSuccess(@RequestParam("orderCode") String orderCode) {
        Payment payment = paymentService.findByOrderCode(orderCode);
        if (payment != null) {
            payment.setStatus(PaymentStatusConstant.SUCCESS);
            paymentService.savePayment(payment);
            
            // Thông báo cho order-service
            paymentService.notifyOrderService(payment);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Không tìm thấy orderCode");
        }
        
        return ResponseEntity.ok("Thanh toán thành công!");
    }
    
    /**
     * Endpoint xử lý khi thanh toán bị hủy
     */
    @GetMapping("/cancel")
    public ResponseEntity<String> paymentCancel(@RequestParam("orderCode") Long orderCode) {
        Payment payment = paymentService.findByOrderCode(String.valueOf(orderCode));
        if (payment != null) {
            try {
                PaymentLinkData paymentLinkData = payOS.cancelPaymentLink(orderCode, "Cancelled by user");
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Không tìm thấy orderCode");
            }
            payment.setStatus(PaymentStatusConstant.CANCEL);
            paymentService.savePayment(payment);
            
            // Thông báo cho order-service
            paymentService.notifyOrderService(payment);
        }
        
        return ResponseEntity.ok("Thanh toán đã bị hủy!");
    }

    /**
     * Lấy thông tin payment theo id
     */
    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        Payment payment = paymentService.findById(id);
        if (payment == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(payment);
    }

    /**
     * Cập nhật payment theo id
     */
    @PutMapping("/{id}")
    public ResponseEntity<Payment> updatePayment(@PathVariable Long id, @RequestBody Payment updatedPayment) {
        Payment payment = paymentService.updatePayment(id, updatedPayment);
        if (payment == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(payment);
    }

    /**
     * Xoá payment theo id
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        boolean deleted = paymentService.deletePayment(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
