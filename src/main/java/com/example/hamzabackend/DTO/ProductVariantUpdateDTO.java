package com.example.hamzabackend.DTO;

import lombok.Data;
import java.util.List;

@Data
public class ProductVariantUpdateDTO {
    private String variantId;
    private String color;
    private List<SizeVariantDTO> sizes;
    private List<String> imagesToRemove;
}