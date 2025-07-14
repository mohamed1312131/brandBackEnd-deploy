package com.example.hamzabackend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SizeVariantDTO {
    private String size;
    private int stock;
    private int sold;
}