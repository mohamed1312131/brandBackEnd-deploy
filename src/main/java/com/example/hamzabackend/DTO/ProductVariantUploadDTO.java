package com.example.hamzabackend.DTO;

import com.example.hamzabackend.entity.SizeVariant;
import lombok.Data;

import java.util.List;

@Data
public class ProductVariantUploadDTO {
    private String color;
    private List<SizeVariant> sizes;
}
