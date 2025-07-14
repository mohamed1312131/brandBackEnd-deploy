package com.example.hamzabackend.repository;
import com.example.hamzabackend.entity.*;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductCategoryRepository extends MongoRepository<ProductCategory, String> {
    Optional<ProductCategory> findByNameIgnoreCase(String name);

    List<ProductCategory> findByStatusTrue();
}
