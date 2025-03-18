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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

            if (product == null) {
                throw new RuntimeException("Product not found: " + item.getProductId());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPricePerUnit(product.getPrice());
            orderItem.setTotalPrice(product.getPrice() * item.getQuantity());

            totalAmount += orderItem.getTotalPrice();
            orderItems.add(orderItem);
        }

        Order order = new Order();
        order.setCustomerName(orderRequest.getCustomerName());
        order.setCustomerEmail(orderRequest.getCustomerEmail());
        order.setCustomerPhone(orderRequest.getCustomerPhone());
        order.setCustomerAddress(orderRequest.getCustomerAddress());
        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());

        orderRepository.save(order);

        return new OrderResponse(order.getId(), order.getStatus(), totalAmount);
    }
}
