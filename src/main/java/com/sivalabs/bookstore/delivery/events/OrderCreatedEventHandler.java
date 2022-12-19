package com.sivalabs.bookstore.delivery.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sivalabs.bookstore.delivery.domain.Order;
import com.sivalabs.bookstore.delivery.domain.OrderRepository;
import com.sivalabs.bookstore.delivery.domain.OrderStatus;
import com.sivalabs.bookstore.delivery.events.model.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedEventHandler {
    private static final Logger log = LoggerFactory.getLogger(OrderCreatedEventHandler.class);
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    public OrderCreatedEventHandler(OrderRepository orderRepository, ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.new-orders-topic}")
    public void handle(String payload) {
        try {
            OrderCreatedEvent event = objectMapper.readValue(payload, OrderCreatedEvent.class);
            log.info("Received a OrderCreatedEvent with orderId:{}: ", event.orderId());
            Order order = new Order();
            order.setOrderId(event.orderId());
            order.setPayload(payload);
            order.setStatus(OrderStatus.READY_TO_SHIP);
            orderRepository.save(order);
            log.info("Saved OrderId: {} with status:{}: ", order.getOrderId(), order.getStatus());
        } catch (RuntimeException | JsonProcessingException e) {
            log.error("Error processing OrderCreatedEvent. Payload: {}", payload);
            log.error(e.getMessage(), e);
        }
    }
}
