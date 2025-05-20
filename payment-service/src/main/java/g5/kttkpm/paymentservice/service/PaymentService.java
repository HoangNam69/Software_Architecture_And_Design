package g5.kttkpm.paymentservice.service;

import g5.kttkpm.paymentservice.entity.Payment;

public interface PaymentService {

    Payment savePayment(Payment payment);
    
    Payment findByOrderCode(String orderCode);
    
    void notifyOrderService(Payment payment);

    Payment findById(Long id);

    Payment updatePayment(Long id, Payment payment);

    boolean deletePayment(Long id);
}
