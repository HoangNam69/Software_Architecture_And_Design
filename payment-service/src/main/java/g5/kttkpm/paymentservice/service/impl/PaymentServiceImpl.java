package g5.kttkpm.paymentservice.service.impl;

import g5.kttkpm.paymentservice.dto.PaymentNotificationDTO;
import g5.kttkpm.paymentservice.entity.Payment;
import g5.kttkpm.paymentservice.repo.PaymentRepository;
import g5.kttkpm.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate;
    
    @Value("${order-service.notification-url:localhost:5179}")
    private String orderServiceNotificationUrl;
    
    @Override
    public Payment savePayment(Payment payment) {
        return paymentRepository.save(payment);
    }
    
    @Override
    public Payment findByOrderCode(String orderCode) {
        return paymentRepository.findByOrderCode(orderCode).orElse(null);
    }
    
    @Override
    public void notifyOrderService(Payment payment) {
        // Tạo đối tượng thông báo
        PaymentNotificationDTO notification = new PaymentNotificationDTO(
            payment.getOrderCode(),
            payment.getStatus(),
            payment.getAmount(),
            payment.getPaymentMethod(),
            payment.getTransactionId()
        );
        
        // Gửi thông báo đến order-service
        try {
            restTemplate.postForEntity(orderServiceNotificationUrl + "/payment/notification", notification, Void.class);
        } catch (Exception e) {
            // Xử lý lỗi khi gửi thông báo
            e.printStackTrace();
            // Có thể triển khai retry mechanism hoặc message queue sau này
        }
    }
}
