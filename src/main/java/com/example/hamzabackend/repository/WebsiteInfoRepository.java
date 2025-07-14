package com.example.hamzabackend.repository;


import com.example.hamzabackend.entity.WebsiteInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WebsiteInfoRepository extends MongoRepository<WebsiteInfo, String> {
}
