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
@Document("checkouts")
public class Checkout {
    @Id
    private String id;
    private String orderId; // <-- hz23232 style
    private String firstName;
    private String lastName;
    private String region;
    private String city;
    private String address;
    private String zipCode;
    private String phone;
    private String email;
    private String note;
    private String status;
    @CreatedDate
    private Instant createdAt;
    private List<OrderedProduct> products;
    private double total;
    private double grandTotal;
    private boolean delivered;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderedProduct {
        private String productId;
        private String productName; // <-- NEW
        private String size;        // <-- Optional, if you use size
        private int quantity;
    }
}
