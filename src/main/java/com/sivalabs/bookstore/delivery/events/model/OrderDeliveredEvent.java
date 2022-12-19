package com.sivalabs.bookstore.delivery.events.model;

import java.util.Set;

public record OrderDeliveredEvent(
        String orderId, Set<OrderItem> items, Customer customer, Address deliveryAddress) {}
