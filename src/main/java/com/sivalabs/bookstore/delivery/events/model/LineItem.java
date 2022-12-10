package com.sivalabs.bookstore.delivery.events.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LineItem {
    private String isbn;
    private String title;
    private BigDecimal price;
    private Integer quantity;
}
