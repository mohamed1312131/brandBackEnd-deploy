package com.example.hamzabackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("carts")
public class Cart {
    @Id
    private String id;
    private List<CartItem> products;
    private double total;
    private double grandTotal;
    @CreatedDate
    private Instant createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItem {
        private String productId;
        private int quantity;
    }
}
