package com.example.hamzabackend.repository;

import com.example.hamzabackend.entity.ProductVariant;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductVariantRepository extends MongoRepository<ProductVariant, String> {
    List<ProductVariant> findByProductId(String productId);
}