package g5.kttkpm.orderservice.service;

import g5.kttkpm.orderservice.dto.OrderRequest;
import g5.kttkpm.orderservice.dto.OrderResponse;


public interface OrderService {
    OrderResponse createOrder(OrderRequest orderRequest);
}
