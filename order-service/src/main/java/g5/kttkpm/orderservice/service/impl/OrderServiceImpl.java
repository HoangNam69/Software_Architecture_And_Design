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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
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
            log.info(product.toString());
            
            // Check if product has enough inventory
            if (product.getTotalQuantity() < item.getQuantity()) {
                throw new RuntimeException("Insufficient inventory for product: " + product.getName() +
                    " (Available: " + product.getTotalQuantity() + ", Requested: " + item.getQuantity() + ")");
            }
            
            BigDecimal productPrice = product.getCurrentPrice() != null ? product.getCurrentPrice() : product.getBasePrice();
            BigDecimal itemTotalPrice = productPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            
            OrderItem orderItem = OrderItem.builder()
                .productId(product.getProductId())
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
        
        // Save the order first to get the order ID
        order = orderRepository.save(order);
        
        // Then update product quantities
        try {
            updateProductInventories(order.getItems());
            log.info("Product inventories successfully updated for order ID: {}", order.getId());
        } catch (Exception e) {
            log.error("Failed to update product inventories for order ID: {}", order.getId(), e);
            throw new RuntimeException("Order created but failed to update product inventories: " + e.getMessage(), e);
        }
        
        return order;
    }
    
    /**
     * Updates product inventories based on order items
     * @param orderItems The list of items in the order
     */
    private void updateProductInventories(List<OrderItem> orderItems) {
        for (OrderItem item : orderItems) {
            ProductDTO product = productClient.getProductById(item.getProductId());
            
            // Calculate new quantity
            int newQuantity = product.getTotalQuantity() - item.getQuantity();
            
            // Update product quantity
            productClient.updateProductInventory(
                item.getProductId(),
                newQuantity,
                "ORDER_PLACEMENT"
            );
            
            log.info("Updated inventory for product ID: {}, new quantity: {}",
                item.getProductId(), newQuantity);
        }
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
    @Transactional
    public void updateOrder(Order order) {
        Order existingOrder = orderRepository.findById(order.getId())
            .orElseThrow(() -> new RuntimeException("Order not found: " + order.getId()));
        
        // If status changed to CANCELLED, restore product quantities
        if ("CANCELLED".equals(order.getStatus()) && !"CANCELLED".equals(existingOrder.getStatus())) {
            restoreProductInventories(existingOrder.getItems());
            log.info("Product inventories restored for cancelled order ID: {}", order.getId());
        }
        
        orderRepository.save(order);
    }
    
    /**
     * Restores product inventories when an order is cancelled
     * @param orderItems The list of items in the cancelled order
     */
    private void restoreProductInventories(List<OrderItem> orderItems) {
        for (OrderItem item : orderItems) {
            ProductDTO product = productClient.getProductById(item.getProductId());
            
            // Calculate new quantity
            int newQuantity = product.getTotalQuantity() + item.getQuantity();
            
            // Update product quantity
            productClient.updateProductInventory(
                item.getProductId(),
                newQuantity,
                "ORDER_CANCELLATION"
            );
            
            log.info("Restored inventory for product ID: {}, new quantity: {}",
                item.getProductId(), newQuantity);
        }
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
