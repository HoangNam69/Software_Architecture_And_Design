package g5.kttkpm.orderservice.service.impl;

import g5.kttkpm.orderservice.client.ProductClient;
import g5.kttkpm.orderservice.dto.OrderRequest;
import g5.kttkpm.orderservice.dto.OrderResponse;
import g5.kttkpm.orderservice.entity.Order;
import g5.kttkpm.orderservice.entity.OrderItem;
import g5.kttkpm.orderservice.repo.OrderRepository;
import g5.kttkpm.orderservice.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest) {
        List<OrderItem> orderItems = new ArrayList<>();
        int totalAmount = 0;

        Order order = Order.builder()
                .customerName(orderRequest.getCustomerName())
                .customerEmail(orderRequest.getCustomerEmail())
                .customerPhone(orderRequest.getCustomerPhone())
                .customerAddress(orderRequest.getCustomerAddress())
                .status("PENDING")
                .build();

        for (var item : orderRequest.getItems()) {
            var product = productClient.getProductById(item.getProductId());

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(item.getQuantity())
                    .pricePerUnit(product.getPrice())
                    .totalPrice(product.getPrice() * item.getQuantity())
                    .build();

            totalAmount += orderItem.getTotalPrice();
            orderItems.add(orderItem);
        }

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);
        order = orderRepository.save(order);

        return new OrderResponse(String.valueOf(order.getId()), order.getStatus(), totalAmount, order.getCustomerName(), order.getCustomerPhone());
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(order -> new OrderResponse(String.valueOf(order.getId()), order.getStatus(), order.getTotalAmount(), order.getCustomerName(), order.getCustomerPhone()))
                .toList();
    }
}