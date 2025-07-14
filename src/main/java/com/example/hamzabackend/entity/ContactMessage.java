package com.example.hamzabackend.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "contact_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactMessage {
    @Id
    private String id;

    private String name;
    private String email;
    private String phone;
    private String subject;
    private String message;
}
