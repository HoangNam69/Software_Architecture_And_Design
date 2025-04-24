package g5.kttkpm.paymentservice.service;

import g5.kttkpm.paymentservice.entity.Payment;

public interface PaymentService {

    Payment savePayment(Payment payment);
    
    Payment findByOrderCode(String orderCode);
    
    void notifyOrderService(Payment payment);
}
