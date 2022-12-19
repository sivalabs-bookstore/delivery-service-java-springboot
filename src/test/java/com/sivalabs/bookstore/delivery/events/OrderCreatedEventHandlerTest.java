package com.sivalabs.bookstore.delivery.events;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.sivalabs.bookstore.delivery.ApplicationProperties;
import com.sivalabs.bookstore.delivery.common.AbstractIntegrationTest;
import com.sivalabs.bookstore.delivery.domain.Order;
import com.sivalabs.bookstore.delivery.domain.OrderRepository;
import com.sivalabs.bookstore.delivery.events.model.Address;
import com.sivalabs.bookstore.delivery.events.model.Customer;
import com.sivalabs.bookstore.delivery.events.model.OrderCreatedEvent;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

class OrderCreatedEventHandlerTest extends AbstractIntegrationTest {
    private static final Logger logger =
            LoggerFactory.getLogger(OrderCreatedEventHandlerTest.class);

    @Autowired private OrderRepository orderRepository;

    @Autowired private KafkaHelper kafkaHelper;

    @Autowired private ApplicationProperties properties;

    @Test
    void shouldHandleOrderCreatedEvent() {
        Customer customer = new Customer("Siva", "siva@gmail.com", "999999999");
        Address address = new Address("addr line 1", null, "Hyderabad", "TS", "500072", "India");
        OrderCreatedEvent event =
                new OrderCreatedEvent(UUID.randomUUID().toString(), Set.of(), customer, address);
        logger.info("Created OrderId:{}", event.orderId());

        kafkaHelper.send(properties.newOrdersTopic(), event);

        await().atMost(30, SECONDS)
                .untilAsserted(
                        () -> {
                            Optional<Order> optionalOrder =
                                    orderRepository.findByOrderId(event.orderId());
                            assertThat(optionalOrder).isNotEmpty();
                            assertThat(optionalOrder.get().getOrderId()).isEqualTo(event.orderId());
                        });
    }
}
