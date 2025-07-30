package com.example.hamzabackend.repository;

import com.example.hamzabackend.entity.Faq;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FaqRepository extends MongoRepository<Faq, String> {
}
