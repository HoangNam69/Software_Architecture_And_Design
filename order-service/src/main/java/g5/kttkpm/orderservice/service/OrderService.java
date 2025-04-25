package g5.kttkpm.orderservice.service;

import g5.kttkpm.orderservice.dto.OrderRequest;
import g5.kttkpm.orderservice.entity.Order;

import java.util.List;

public interface OrderService {
    Order createOrder(OrderRequest orderRequest);
    List<Order> getAllOrders();
    Order getOrderById(Long orderId);
    
    void updatePaymentUrlAndStatusByPaymentOrderCode(Order order);
    
    Order findByPaymentOrderCode(String s);
    
    void updateOrder(Order order);
}
