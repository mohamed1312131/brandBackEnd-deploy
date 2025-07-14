package com.example.hamzabackend.repository;

import com.example.hamzabackend.DTO.TopSellingProductDTO;

import java.util.List;

public interface OrderRepositoryCustom {
    List<TopSellingProductDTO> findTopSellingProducts(int year, int month, int limit);
}
