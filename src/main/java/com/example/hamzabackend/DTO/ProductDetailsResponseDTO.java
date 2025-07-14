package com.example.hamzabackend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProductDetailsResponseDTO {
    private String id;
    private String title;
    private String description;

    private List<CategoryDTO> allCategories;


    private double price;
    private Double oldPrice;
    private String additionalInfo;
    private String sizeGuide;              // URL to size guide image
    private List<VariantDTO> variants;

    @Data
    @AllArgsConstructor
    public static class VariantDTO {
        private String variantId;      // new field
        private String color;
        private List<String> images;
        private List<SizeStockDTO> sizes;
    }

    @Data
    @AllArgsConstructor
    public static class SizeStockDTO {
        private String size;
        private int stock;        // Current remaining stock
        private int sold;         // Total sold quantity
        private int totalStock;   // Original total stock (stock + sold)
    }
}