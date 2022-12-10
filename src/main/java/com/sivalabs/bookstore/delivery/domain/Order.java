package com.sivalabs.bookstore.delivery.domain;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orders")
public class Order {
    @Id private String id;

    @Indexed(unique = true)
    @NotEmpty(message = "OrderId must not be null/empty")
    private String orderId;

    @NotEmpty(message = "Order payload must not be null/empty")
    private String payload;

    private OrderStatus status;

    public enum OrderStatus {
        READY_TO_SHIP,
        DELIVERED,
        CANCELLED,
        ERROR
    }
}
