package com.sivalabs.bookstore.delivery.events.model;

import java.util.Set;

public record OrderErrorEvent(
        String orderId,
        Set<OrderItem> items,
        Customer customer,
        Address deliveryAddress,
        String reason) {}
