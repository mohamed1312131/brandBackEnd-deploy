package com.example.hamzabackend.controller;

import com.example.hamzabackend.DTO.ClientInquiryRequest;
import com.example.hamzabackend.entity.ClientInquiry;
import com.example.hamzabackend.service.ClientInquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Only POST is public (see SecurityConfig); listing, status changes and deletes require an admin session.
@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
public class ClientInquiryController {

    private final ClientInquiryService inquiryService;

    public record StatusUpdate(ClientInquiry.Status status) {}

    @PostMapping
    public ResponseEntity<Void> submit(@RequestBody ClientInquiryRequest request) {
        inquiryService.submit(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public List<ClientInquiry> getAll() {
        return inquiryService.getAll();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ClientInquiry> updateStatus(@PathVariable String id, @RequestBody StatusUpdate body) {
        return inquiryService.updateStatus(id, body == null ? null : body.status())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        return inquiryService.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
