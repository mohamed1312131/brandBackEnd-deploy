package com.example.hamzabackend.DTO;


import lombok.Data;
import java.util.List;

@Data
public class ProductDetailsDTO {
    private String title;
    private String description;

    // User can send one of these, but not both.
    private String categoryId;       // For selecting an existing category
    private String newCategoryName;  // For creating a new one

    private double price;
    private Double oldPrice;
    private String additionalInfo;
    private String sizeGuide;              // URL to size guide image
    private List<String> variantsToDelete; // 

}