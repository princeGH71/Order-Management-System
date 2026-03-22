package com.prince.order_management_api.repository;

import com.prince.order_management_api.entity.Order;
import com.prince.order_management_api.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByUserId(UUID id);
    List<Order> findByStatus(OrderStatus status);
}
