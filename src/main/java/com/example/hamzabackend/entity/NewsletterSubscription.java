package com.example.hamzabackend.entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("newsletter_subscriptions")
public class NewsletterSubscription {
    @Id
    @Indexed(unique = true)
    private String email;
    private Instant subscribedAt;

    public NewsletterSubscription(String email) {
        this.email= email;
    }
}