package com.sivalabs.bookstore.delivery.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByStatus(Order.OrderStatus status);

    Optional<Order> findByOrderId(String orderId);
}
