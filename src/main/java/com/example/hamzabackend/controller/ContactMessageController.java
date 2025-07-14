package com.example.hamzabackend.controller;

import com.example.hamzabackend.entity.ContactMessage;
import com.example.hamzabackend.repository.ContactMessageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contact")
// CORS is handled globally in SecurityConfig
public class ContactMessageController {

    private final ContactMessageRepository repository;

    public ContactMessageController(ContactMessageRepository repository) {
        this.repository = repository;
    }

    // Create a new message
    @PostMapping
    public ResponseEntity<ContactMessage> createMessage(@RequestBody ContactMessage message) {
        return ResponseEntity.ok(repository.save(message));
    }

    // Get all messages
    @GetMapping
    public ResponseEntity<List<ContactMessage>> getAllMessages() {
        return ResponseEntity.ok(repository.findAll());
    }

    // Delete a message by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable String id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
