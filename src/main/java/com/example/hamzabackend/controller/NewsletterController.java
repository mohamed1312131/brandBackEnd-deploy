package com.example.hamzabackend.controller;


import com.example.hamzabackend.entity.NewsletterSubscription;
import com.example.hamzabackend.service.CloudinaryService;
import com.example.hamzabackend.service.NewsletterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/newsletter")
public class NewsletterController {

    private final CloudinaryService cloudinaryService;
    private final NewsletterService service;

    public NewsletterController(NewsletterService service,CloudinaryService cloudinaryService) {
        this.service = service;
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribe(@RequestParam("email") String email) {
        service.subscribe(email);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public List<NewsletterSubscription> getAll() {
        return service.getAllSubscribers();
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<Void> unsubscribe(@PathVariable String email) {
        service.unsubscribe(email);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/upload-image")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        String imageUrl = cloudinaryService.uploadImage(file);
        return ResponseEntity.ok(imageUrl);
    }

    @PostMapping("/send")
    public ResponseEntity<Void> sendCustomEmail(@RequestBody Map<String, Object> payload) {
        List<String> emails = (List<String>) payload.get("emails");
        String subject = (String) payload.get("subject");
        String title = (String) payload.get("title");
        String subtitle = (String) payload.get("subtitle");
        String body = (String) payload.get("body");
        String imageUrl = (String) payload.get("imageUrl");
        String productLink = (String) payload.get("productLink");

        service.sendEmailToSubscribers(emails, subject, title, subtitle, body, imageUrl, productLink);
        return ResponseEntity.ok().build();
    }


}

