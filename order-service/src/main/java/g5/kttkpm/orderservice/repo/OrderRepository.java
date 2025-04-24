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
    @Query("update Order o set o.paymentOrderCode = ?1, o.paymentUrl = ?2, o.status = ?3")
    void updatePaymentOrderCodeAndPaymentUrlAndStatusBy(String paymentOrderCode, String paymentUrl, String status);
    
    Order findByPaymentOrderCode(String paymentOrderCode);
}
