package com.example.hamzabackend.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document("faqs")
public class Faq {
    @Id
    private String id;

    private String question;
    private String answer;

    @CreatedDate
    private Instant createdAt;
}
