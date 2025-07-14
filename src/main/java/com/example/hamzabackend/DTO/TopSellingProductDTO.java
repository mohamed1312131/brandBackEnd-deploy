package com.example.hamzabackend.DTO;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// TopSellingProductDTO.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopSellingProductDTO {
    private String productId;
    private String productName;
    private String imageUrl;
    private int totalSalesCount;
    private double totalRevenue;
    private int totalStock;
    private Map<String, Integer> stockLeftBySize; // size -> stock left
}
