package com.prince.order_management_api.kafka;

import com.prince.order_management_api.config.KafkaConfig;
import com.prince.order_management_api.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public void publishOrderPlaced(OrderPlacedEvent event) {
        kafkaTemplate.send(KafkaConfig.ORDER_TOPIC, event.getOrderId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Order event published — orderId: {}, offset: {}",
                                event.getOrderId(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to publish order event — orderId: {}",
                                event.getOrderId(), ex);
                    }
                });
    }
}