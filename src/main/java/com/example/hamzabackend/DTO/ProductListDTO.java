package com.example.hamzabackend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProductListDTO {
    private String id;
    private String title;
    private String description;
    private String thumbnail;
    private List<String> sizes;
    private List<String> colors;
    private double price;
    private long stockLeft;
    private long sold;
    private String category;
}