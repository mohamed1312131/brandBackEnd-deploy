package com.example.hamzabackend.entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("notes")
public class Note {
    @Id
    private String id;
    private String imageUrl;
    private String title;
    private String description;
    private boolean status = true;
    @CreatedDate
    private Instant createdAt;
}