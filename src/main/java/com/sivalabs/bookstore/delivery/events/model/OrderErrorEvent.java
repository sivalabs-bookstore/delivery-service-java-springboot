package com.sivalabs.bookstore.delivery.events.model;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderErrorEvent {
    private String orderId;
    private String reason;
    private Set<LineItem> items;
    private Customer customer;
    private Address deliveryAddress;
}
