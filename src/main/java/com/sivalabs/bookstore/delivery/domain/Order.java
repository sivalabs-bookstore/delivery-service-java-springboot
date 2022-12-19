package com.sivalabs.bookstore.delivery.domain;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "orders")
public class Order {
    @Id private String id;

    @Indexed(unique = true)
    @NotEmpty(message = "OrderId must not be null/empty")
    private String orderId;

    @NotEmpty(message = "Order payload must not be null/empty")
    private String payload;

    private OrderStatus status;

    public Order(String id, String orderId, String payload, OrderStatus status) {
        this.id = id;
        this.orderId = orderId;
        this.payload = payload;
        this.status = status;
    }

    public Order() {}

    public String getId() {
        return this.id;
    }

    public String getOrderId() {
        return this.orderId;
    }

    public String getPayload() {
        return this.payload;
    }

    public OrderStatus getStatus() {
        return this.status;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
