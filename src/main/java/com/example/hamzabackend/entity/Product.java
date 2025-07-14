package com.example.hamzabackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

// src/main/java/com/example/hamzabackend/entity/Product.java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("products")
public class Product {
    @Id
    private String id;
    private String title;
    private String description;

    @DBRef
    private ProductCategory category;


    private double price;
    private Double oldPrice;
    private Double discountPercent;       // new field
    private String additionalInfo;
    private String sizeGuide;             // URL to size guide image
    @DBRef
    private List<ProductVariant> variants;

    @CreatedDate
    private Instant createdAt;
}
