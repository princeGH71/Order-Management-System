package com.prince.order_management_api.service;

import com.prince.order_management_api.dto.request.OrderRequest;
import com.prince.order_management_api.dto.response.OrderResponse;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderResponse placeOrder(OrderRequest request, String email);
    OrderResponse getOrderById(UUID id, String email);
    List<OrderResponse> getMyOrders(String email);
    OrderResponse cancelOrder(UUID id, String email);
}
