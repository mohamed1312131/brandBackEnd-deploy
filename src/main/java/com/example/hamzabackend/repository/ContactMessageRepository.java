package com.example.hamzabackend.repository;
import com.example.hamzabackend.entity.*;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ContactMessageRepository extends MongoRepository<ContactMessage, String> {}
