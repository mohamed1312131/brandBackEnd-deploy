package com.example.hamzabackend.repository;
import com.example.hamzabackend.entity.*;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends MongoRepository<Note, String> {
    List<Note> findByStatusTrue();
}
