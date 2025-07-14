package com.example.hamzabackend.repository;

import com.example.hamzabackend.entity.Visitor;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface VisitorRepository extends MongoRepository<Visitor, String> {
}
