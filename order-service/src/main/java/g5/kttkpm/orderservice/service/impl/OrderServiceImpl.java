package g5.kttkpm.orderservice.service.impl;

import g5.kttkpm.orderservice.client.ProductClient;
import g5.kttkpm.orderservice.dto.OrderItemDTO;
import g5.kttkpm.orderservice.dto.OrderRequest;
import g5.kttkpm.orderservice.dto.OrderResponse;
import g5.kttkpm.orderservice.dto.ProductDTO;
import g5.kttkpm.orderservice.entity.Order;
import g5.kttkpm.orderservice.entity.OrderItem;
import g5.kttkpm.orderservice.repo.OrderRepository;
import g5.kttkpm.orderservice.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

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
        // new OrderResponse(order.getId(), order.getStatus(), totalAmount, order.getCustomerName(), order.getCustomerPhone());
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
        //.stream()
        //                .map(order -> new OrderResponse(order.getId(), order.getStatus(), order.getTotalAmount(), order.getCustomerName(), order.getCustomerPhone()))
        //                .toList();
    }
    
    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElse(null);
        
//        return OrderResponse.builder()
//            .id(order.getId())
//            .status(order.getStatus())
//            .totalAmount(order.getTotalAmount())
//            .customerName(order.getCustomerName())
//            .customerPhone(order.getCustomerPhone())
//            .build();
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
}
