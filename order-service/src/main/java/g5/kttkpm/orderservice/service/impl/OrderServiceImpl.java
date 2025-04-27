package g5.kttkpm.orderservice.service.impl;

import g5.kttkpm.orderservice.client.ProductClient;
import g5.kttkpm.orderservice.dto.OrderItemDTO;
import g5.kttkpm.orderservice.dto.OrderRequest;
import g5.kttkpm.orderservice.dto.ProductDTO;
import g5.kttkpm.orderservice.entity.Order;
import g5.kttkpm.orderservice.entity.OrderItem;
import g5.kttkpm.orderservice.repo.OrderRepository;
import g5.kttkpm.orderservice.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    @Override
    @Transactional
    public Order createOrder(OrderRequest orderRequest) {
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        Order order = Order.builder()
                .userId(orderRequest.getUserId())
                .customerName(orderRequest.getCustomerName())
                .customerEmail(orderRequest.getCustomerEmail())
                .customerPhone(orderRequest.getCustomerPhone())
                .customerAddress(orderRequest.getCustomerAddress())
                .status("PENDING")
                .build();

        for (OrderItemDTO item : orderRequest.getItems()) {
            ProductDTO product = productClient.getProductById(item.getProductId());

            if (product == null) {
                throw new RuntimeException("Product not found: " + item.getProductId());
            }

            BigDecimal productPrice = product.getCurrentPrice();
            BigDecimal itemTotalPrice = productPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(item.getQuantity())
                    .pricePerUnit(productPrice)
                    .totalPrice(itemTotalPrice)
                    .build();

            totalAmount = totalAmount.add(itemTotalPrice);
            orderItems.add(orderItem);
        }

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);
        order = orderRepository.save(order);

        return order;
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    @Override
    public void updatePaymentUrlAndStatusByPaymentOrderCode(Order order) {
        orderRepository.updatePaymentUrlAndStatusByPaymentOrderCode(
                order.getPaymentOrderCode(),
                order.getPaymentUrl(),
                order.getStatus());
    }

    @Override
    public Order findByPaymentOrderCode(String paymentOrderCode) {
        return orderRepository.findByPaymentOrderCode(paymentOrderCode);
    }

    @Override
    public void updateOrder(Order order) {
        orderRepository.save(order);
    }

    @Override
    public void deleteOrderById(Long id) {
        orderRepository.deleteById(id);
    }

    @Override
    public List<Order> getOrderByUserId(String userId) {
        return orderRepository.findAllByUserId(userId);
    }
}
