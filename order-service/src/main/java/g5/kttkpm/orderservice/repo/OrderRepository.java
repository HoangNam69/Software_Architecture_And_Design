package g5.kttkpm.orderservice.repo;

import g5.kttkpm.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    @Transactional
    @Modifying
    @Query("update Order o set o.paymentUrl = ?1, o.status = ?2 where o.paymentOrderCode = ?3")
    void updatePaymentUrlAndStatusByPaymentOrderCode(String paymentUrl, String status, String paymentOrderCode);
    
    
    Order findByPaymentOrderCode(String paymentOrderCode);
}
