package com.example.hamzabackend.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document("product_categories")
public class ProductCategory {
    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    private String imageUrl;

    private boolean status = true; // Active by default
}
