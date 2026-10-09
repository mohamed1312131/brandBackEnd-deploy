package com.example.hamzabackend.repository;

import com.example.hamzabackend.entity.ClientInquiry;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ClientInquiryRepository extends MongoRepository<ClientInquiry, String> {
    List<ClientInquiry> findAllByOrderByCreatedAtDesc();
}
