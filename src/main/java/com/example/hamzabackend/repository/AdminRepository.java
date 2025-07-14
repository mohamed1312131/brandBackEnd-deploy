package com.example.hamzabackend.repository;


import com.example.hamzabackend.entity.Admin;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AdminRepository extends MongoRepository<Admin, String> {
    Optional<Admin> findByEmail(String email);
    boolean existsByEmail(String email);
}
