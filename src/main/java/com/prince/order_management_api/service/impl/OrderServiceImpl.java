package com.prince.order_management_api.service.impl;

import com.prince.order_management_api.dto.request.OrderItemRequest;
import com.prince.order_management_api.dto.request.OrderRequest;
import com.prince.order_management_api.dto.response.OrderItemResponse;
import com.prince.order_management_api.dto.response.OrderResponse;
import com.prince.order_management_api.entity.Order;
import com.prince.order_management_api.entity.OrderItem;
import com.prince.order_management_api.entity.Product;
import com.prince.order_management_api.entity.User;
import com.prince.order_management_api.enums.OrderStatus;
import com.prince.order_management_api.exception.ApiException;
import com.prince.order_management_api.repository.OrderRepository;
import com.prince.order_management_api.repository.ProductRepository;
import com.prince.order_management_api.repository.UserRepository;
import com.prince.order_management_api.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public OrderResponse placeOrder(OrderRequest request, String email) {
//        fetch user placing order
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new ApiException("User not found", HttpStatus.NOT_FOUND)
        );
        
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for(OrderItemRequest itemRequest: request.getItems()){
//            find product or orderItem
            Product product = productRepository.findById(itemRequest.getProductId()).orElseThrow(
                    () -> new ApiException("Product not found", HttpStatus.NOT_FOUND)
            );
            
//            validate stock of that product
            if(product.getStock() < itemRequest.getQuantity()){
                throw new ApiException(
                        "Insufficient stock for product: " + product.getName(),
                        HttpStatus.BAD_REQUEST
                );
            }
            
//            lock the price
            BigDecimal itemTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            
            totalAmount = totalAmount.add(itemTotal);
            
//            reduce stock
            product.setStock(product.getStock() - itemRequest.getQuantity());
            productRepository.save(product);
            
//            build order item - no order set yet, we set it later
            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .price(product.getPrice())
                    .build();
            orderItems.add(orderItem);
        }
        
//        build the order
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PLACED)
                .totalAmount(totalAmount)
                .items(new ArrayList<>())
                .build();
        
//        link each orderItem to order
        for(OrderItem item: orderItems){
            item.setOrder(order);
            order.getItems().add(item);
        }
        
//        save order - cascades and saves all items too
        Order saved = orderRepository.save(order);
        return mapToResponse(saved);
    }

    @Override
    public OrderResponse getOrderById(UUID id, String email) {
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new ApiException("Order not found.", HttpStatus.NOT_FOUND)
        );
        
//        user can only see there own order
        if(!order.getUser().getEmail().equals(email)){
            throw new ApiException("Access denied", HttpStatus.FORBIDDEN);
        }
        
        return mapToResponse(order);
    }

    @Override
    public List<OrderResponse> getMyOrders(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new ApiException("User not found", HttpStatus.NOT_FOUND)
        );
        
        return orderRepository.findByUserId(user.getId())
                .stream()
                .map(order -> mapToResponse(order))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(UUID id, String email) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new ApiException("Order not found", HttpStatus.NOT_FOUND));
//        only order owner can cancel
        if(!order.getUser().getEmail().equals(email)){
            throw new ApiException("Access denied", HttpStatus.FORBIDDEN);
        }
        
//        only placed orders can be cancelled
        if(order.getStatus() != OrderStatus.PLACED){
            throw new ApiException("Only placed orders can be cancelled", HttpStatus.BAD_REQUEST);
        }
        
//        restore stock for each order
        for(OrderItem item: order.getItems()){
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        return mapToResponse(orderRepository.save(order));
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> responses = order.getItems()
                .stream().map(item -> OrderItemResponse.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build()
                )
                .collect(Collectors.toList());
        
        return OrderResponse.builder()
                .id(order.getId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .items(responses)
                .createdAt(order.getCreatedAt())
                .build();
    }
}
