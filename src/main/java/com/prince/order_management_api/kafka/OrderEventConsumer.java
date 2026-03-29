package com.prince.order_management_api.kafka;

import com.prince.order_management_api.event.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderEventConsumer {

    @KafkaListener(
            topics = "order-placed",
            groupId = "order-group1"
    )
    public void consumeOrderPlacedEvent(OrderPlacedEvent event) {
        // In real world — send email, push notification, trigger warehouse
        // For now we log it to simulate processing
        log.info("──────────────────────────────────────────");
        log.info("New order received!");
        log.info("Order ID    : {}", event.getOrderId());
        log.info("User        : {}", event.getUserEmail());
        log.info("Total Amount: ₹{}", event.getTotalAmount());
        log.info("Items       : {}", event.getItemCount());
        log.info("──────────────────────────────────────────");
    }
}