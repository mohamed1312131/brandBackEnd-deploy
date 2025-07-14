package com.example.hamzabackend.repository;


import com.example.hamzabackend.entity.Carousel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarouselRepository extends MongoRepository<Carousel, String> {
    List<Carousel> findByStatusTrue();
}