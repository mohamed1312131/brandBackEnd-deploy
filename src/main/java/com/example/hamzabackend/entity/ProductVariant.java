package com.example.hamzabackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("variants")
public class ProductVariant {
    @Id
    private String id;
    private String color;
    private List<String> images; // URLs from Cloudinary
    private List<SizeVariant> sizes;
    private String productId;
}