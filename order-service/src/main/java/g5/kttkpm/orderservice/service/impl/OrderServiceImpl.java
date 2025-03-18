package g5.kttkpm.orderservice.service.impl;

import g5.kttkpm.orderservice.client.ProductClient;
import g5.kttkpm.orderservice.dto.*;
import g5.kttkpm.orderservice.entity.*;
import g5.kttkpm.orderservice.repo.*;
import g5.kttkpm.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    @Override
    public OrderResponse createOrder(OrderRequest orderRequest) {
        List<OrderItem> orderItems = new ArrayList<>();
        int totalAmount = 0;

        for (OrderItemDTO item : orderRequest.getItems()) {
            ProductDTO product = productClient.getProductById(item.getProductId());

            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(item.getQuantity())
                    .pricePerUnit(product.getPrice())
                    .totalPrice(product.getPrice() * item.getQuantity())
                    .build();

            totalAmount += orderItem.getTotalPrice();
            orderItems.add(orderItem);
        }

        Order order = Order.builder()
                .id(UUID.randomUUID().toString())
                .customerName(orderRequest.getCustomerName())
                .customerEmail(orderRequest.getCustomerEmail())
                .customerPhone(orderRequest.getCustomerPhone())
                .customerAddress(orderRequest.getCustomerAddress())
                .totalAmount(totalAmount)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .items(orderItems)
                .build();

        orderRepository.save(order);

        return new OrderResponse(order.getId(), order.getStatus(), totalAmount, order.getCustomerName(), order.getCustomerPhone());
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();

        return orders.stream().map(order -> new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCustomerName(),
                order.getCustomerPhone()
        )).toList();
    }
}