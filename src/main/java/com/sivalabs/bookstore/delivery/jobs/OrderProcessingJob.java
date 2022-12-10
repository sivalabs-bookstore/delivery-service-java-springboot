package com.sivalabs.bookstore.delivery.jobs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sivalabs.bookstore.delivery.domain.DeliveryService;
import com.sivalabs.bookstore.delivery.domain.Order;
import com.sivalabs.bookstore.delivery.domain.OrderRepository;
import com.sivalabs.bookstore.delivery.events.model.OrderCreatedEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderProcessingJob {
    private final OrderRepository orderRepository;
    private final DeliveryService deliveryService;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 60000)
    public void processOrders() {
        List<Order> orders = orderRepository.findByStatus(Order.OrderStatus.READY_TO_SHIP);
        if (orders.isEmpty()) {
            return;
        }
        for (Order order : orders) {
            OrderCreatedEvent orderCreatedEvent = getOrderCreatedEvent(order);
            deliveryService.process(orderCreatedEvent);
        }
    }

    private OrderCreatedEvent getOrderCreatedEvent(Order order) {
        try {
            String payload = order.getPayload();
            return objectMapper.readValue(payload, OrderCreatedEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
