package com.sivalabs.bookstore.delivery.domain;

import com.sivalabs.bookstore.delivery.ApplicationProperties;
import com.sivalabs.bookstore.delivery.events.KafkaHelper;
import com.sivalabs.bookstore.delivery.events.model.OrderCancelledEvent;
import com.sivalabs.bookstore.delivery.events.model.OrderCreatedEvent;
import com.sivalabs.bookstore.delivery.events.model.OrderDeliveredEvent;
import com.sivalabs.bookstore.delivery.events.model.OrderErrorEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeliveryService {
    private static final List<String> DELIVERY_ALLOWED_COUNTRIES =
            List.of("INDIA", "USA", "GERMANY", "UK");

    private final OrderRepository orderRepository;
    private final KafkaHelper kafkaHelper;
    private final ApplicationProperties properties;

    public void process(OrderCreatedEvent event) {
        try {
            if (canBeDelivered(event)) {
                this.updateOrderStatus(event.getOrderId(), Order.OrderStatus.DELIVERED);
                kafkaHelper.send(
                        properties.deliveredOrdersTopic(), buildOrderDeliveredEvent(event));
            } else {
                this.updateOrderStatus(event.getOrderId(), Order.OrderStatus.CANCELLED);
                kafkaHelper.send(
                        properties.cancelledOrdersTopic(),
                        buildOrderCancelledEvent(event, "Can't deliver to the location"));
            }
        } catch (RuntimeException e) {
            this.updateOrderStatus(event.getOrderId(), Order.OrderStatus.ERROR);
            kafkaHelper.send(
                    properties.cancelledOrdersTopic(), buildOrderErrorEvent(event, e.getMessage()));
        }
    }

    private void updateOrderStatus(String orderId, Order.OrderStatus status) {
        Order order = orderRepository.findByOrderId(orderId).orElseThrow();
        order.setStatus(status);
        orderRepository.save(order);
    }

    private boolean canBeDelivered(OrderCreatedEvent order) {
        return DELIVERY_ALLOWED_COUNTRIES.contains(
                order.getDeliveryAddress().getCountry().toUpperCase());
    }

    private OrderDeliveredEvent buildOrderDeliveredEvent(OrderCreatedEvent order) {
        OrderDeliveredEvent event = new OrderDeliveredEvent();
        event.setOrderId(order.getOrderId());
        event.setCustomer(order.getCustomer());
        event.setDeliveryAddress(order.getDeliveryAddress());
        event.setItems(order.getItems());
        return event;
    }

    private OrderCancelledEvent buildOrderCancelledEvent(OrderCreatedEvent order, String reason) {
        OrderCancelledEvent event = new OrderCancelledEvent();
        event.setOrderId(order.getOrderId());
        event.setReason(reason);
        event.setCustomer(order.getCustomer());
        event.setDeliveryAddress(order.getDeliveryAddress());
        event.setItems(order.getItems());
        return event;
    }

    private OrderErrorEvent buildOrderErrorEvent(OrderCreatedEvent order, String reason) {
        OrderErrorEvent event = new OrderErrorEvent();
        event.setOrderId(order.getOrderId());
        event.setReason(reason);
        event.setCustomer(order.getCustomer());
        event.setDeliveryAddress(order.getDeliveryAddress());
        event.setItems(order.getItems());
        return event;
    }
}
