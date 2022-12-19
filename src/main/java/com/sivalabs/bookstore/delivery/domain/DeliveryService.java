package com.sivalabs.bookstore.delivery.domain;

import com.sivalabs.bookstore.delivery.ApplicationProperties;
import com.sivalabs.bookstore.delivery.events.KafkaHelper;
import com.sivalabs.bookstore.delivery.events.model.OrderCancelledEvent;
import com.sivalabs.bookstore.delivery.events.model.OrderCreatedEvent;
import com.sivalabs.bookstore.delivery.events.model.OrderDeliveredEvent;
import com.sivalabs.bookstore.delivery.events.model.OrderErrorEvent;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DeliveryService {
    private static final Logger logger = LoggerFactory.getLogger(DeliveryService.class);
    private static final List<String> DELIVERY_ALLOWED_COUNTRIES =
            List.of("INDIA", "USA", "GERMANY", "UK");

    private final OrderRepository orderRepository;
    private final KafkaHelper kafkaHelper;
    private final ApplicationProperties properties;

    public DeliveryService(
            OrderRepository orderRepository,
            KafkaHelper kafkaHelper,
            ApplicationProperties properties) {
        this.orderRepository = orderRepository;
        this.kafkaHelper = kafkaHelper;
        this.properties = properties;
    }

    public void process(OrderCreatedEvent event) {
        try {
            if (canBeDelivered(event)) {
                logger.info("OrderId: {} can be delivered", event.orderId());
                this.updateOrderStatus(event.orderId(), OrderStatus.DELIVERED);
                kafkaHelper.send(
                        properties.deliveredOrdersTopic(), buildOrderDeliveredEvent(event));
                logger.info("Published OrderDelivered event with OrderId: {}", event.orderId());
            } else {
                logger.info("OrderId: {} can not be delivered", event.orderId());
                this.updateOrderStatus(event.orderId(), OrderStatus.CANCELLED);
                kafkaHelper.send(
                        properties.cancelledOrdersTopic(),
                        buildOrderCancelledEvent(event, "Can't deliver to the location"));
                logger.info("Published OrderCancelled event with OrderId: {}", event.orderId());
            }
        } catch (RuntimeException e) {
            logger.error("Failed to process OrderCreatedEvent with orderId: " + event.orderId(), e);
            this.updateOrderStatus(event.orderId(), OrderStatus.ERROR);
            kafkaHelper.send(
                    properties.errorOrdersTopic(), buildOrderErrorEvent(event, e.getMessage()));
            logger.info("Published OrderError event with OrderId: {}", event.orderId());
        }
    }

    private void updateOrderStatus(String orderId, OrderStatus status) {
        Order order = orderRepository.findByOrderId(orderId).orElseThrow();
        order.setStatus(status);
        orderRepository.save(order);
    }

    private boolean canBeDelivered(OrderCreatedEvent order) {
        return DELIVERY_ALLOWED_COUNTRIES.contains(order.deliveryAddress().country().toUpperCase());
    }

    private OrderDeliveredEvent buildOrderDeliveredEvent(OrderCreatedEvent order) {
        return new OrderDeliveredEvent(
                order.orderId(), order.items(), order.customer(), order.deliveryAddress());
    }

    private OrderCancelledEvent buildOrderCancelledEvent(OrderCreatedEvent order, String reason) {
        return new OrderCancelledEvent(
                order.orderId(), order.items(), order.customer(), order.deliveryAddress(), reason);
    }

    private OrderErrorEvent buildOrderErrorEvent(OrderCreatedEvent order, String reason) {
        return new OrderErrorEvent(
                order.orderId(), order.items(), order.customer(), order.deliveryAddress(), reason);
    }
}
