package com.example.hamzabackend.controller;

import com.example.hamzabackend.entity.ProductCategory;
import com.example.hamzabackend.service.ProductCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class ProductCategoryController {

    private final ProductCategoryService service;

    public ProductCategoryController(ProductCategoryService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ProductCategory> create(
            @RequestParam String name,
            @RequestParam MultipartFile image
    ) {
        try {
            return ResponseEntity.ok(service.create(name, image));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ProductCategory>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<ProductCategory>> getActive() {
        return ResponseEntity.ok(service.getActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductCategory> getById(@PathVariable String id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/enable")
    public ResponseEntity<ProductCategory> enable(@PathVariable String id) {
        return ResponseEntity.ok(service.enable(id));
    }

    @PutMapping("/{id}/disable")
    public ResponseEntity<ProductCategory> disable(@PathVariable String id) {
        return ResponseEntity.ok(service.disable(id));
    }

    @PostMapping("/{id}")
    public ResponseEntity<ProductCategory> update(
            @PathVariable String id,
            @RequestParam String name,
            @RequestParam(required = false) MultipartFile image
    ) {
        try {
            return ResponseEntity.ok(service.update(id, name, image));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
