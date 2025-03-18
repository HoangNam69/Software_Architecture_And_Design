package g5.kttkpm.orderservice.service;

import g5.kttkpm.orderservice.dto.OrderRequest;
import g5.kttkpm.orderservice.dto.OrderResponse;
import java.util.List;

public interface OrderService {
    OrderResponse createOrder(OrderRequest orderRequest);
    List<OrderResponse> getAllOrders();
}