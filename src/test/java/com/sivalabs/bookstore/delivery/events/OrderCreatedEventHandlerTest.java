package com.sivalabs.bookstore.delivery.events;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.sivalabs.bookstore.delivery.ApplicationProperties;
import com.sivalabs.bookstore.delivery.common.AbstractIntegrationTest;
import com.sivalabs.bookstore.delivery.domain.Order;
import com.sivalabs.bookstore.delivery.domain.OrderRepository;
import com.sivalabs.bookstore.delivery.events.model.Customer;
import com.sivalabs.bookstore.delivery.events.model.OrderCreatedEvent;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
class OrderCreatedEventHandlerTest extends AbstractIntegrationTest {

    @Autowired private OrderRepository orderRepository;

    @Autowired private KafkaHelper kafkaHelper;

    @Autowired private ApplicationProperties properties;

    @Test
    void shouldHandleOrderCreatedEvent() {
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId(UUID.randomUUID().toString());
        event.setCustomer(new Customer());
        event.getCustomer().setName("Siva");
        event.getCustomer().setEmail("siva@gmail.com");
        log.info("Created OrderId:{}", event.getOrderId());

        kafkaHelper.send(properties.newOrdersTopic(), event);

        await().atMost(30, SECONDS)
                .untilAsserted(
                        () -> {
                            Optional<Order> optionalOrder =
                                    orderRepository.findByOrderId(event.getOrderId());
                            assertThat(optionalOrder).isNotEmpty();
                            assertThat(optionalOrder.get().getOrderId())
                                    .isEqualTo(event.getOrderId());
                        });
    }
}
