package com.example.hamzabackend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class ProductUpdateDTO {
    private String title;
    private String description;
    private Double price;
    private Double oldPrice;
    private String additionalInfo;
    private String categoryId;
    private String newCategoryName;
    // optional list of variant updates
    private List<ProductVariantUpdateDTO> variants;
}