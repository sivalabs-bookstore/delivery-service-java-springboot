package com.sivalabs.bookstore.delivery.jobs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sivalabs.bookstore.delivery.domain.DeliveryService;
import com.sivalabs.bookstore.delivery.domain.Order;
import com.sivalabs.bookstore.delivery.domain.OrderRepository;
import com.sivalabs.bookstore.delivery.domain.OrderStatus;
import com.sivalabs.bookstore.delivery.events.model.OrderCreatedEvent;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class OrderProcessingJob {
    private static final Logger log = LoggerFactory.getLogger(OrderProcessingJob.class);
    private final OrderRepository orderRepository;
    private final DeliveryService deliveryService;
    private final ObjectMapper objectMapper;

    public OrderProcessingJob(
            OrderRepository orderRepository,
            DeliveryService deliveryService,
            ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.deliveryService = deliveryService;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 60000)
    public void processOrders() {
        log.info("Processing READY_TO_SHIP orders");
        List<Order> orders = orderRepository.findByStatus(OrderStatus.READY_TO_SHIP);
        if (orders.isEmpty()) {
            return;
        }
        log.info("Found {} READY_TO_SHIP orders", orders.size());
        for (Order order : orders) {
            log.info("Processing OrderId: {}", order.getOrderId());
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
