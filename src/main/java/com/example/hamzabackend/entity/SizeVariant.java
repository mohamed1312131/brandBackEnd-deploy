package com.example.hamzabackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SizeVariant {
    private String size;
    private int stock;
    private int sold;
}