package com.example.hamzabackend.service;

import com.example.hamzabackend.entity.ProductCategory;
import com.example.hamzabackend.repository.ProductCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ProductCategoryService {

    private final ProductCategoryRepository repository;
    private final CloudinaryService cloudinaryService;

    public ProductCategoryService(ProductCategoryRepository repository, CloudinaryService cloudinaryService) {
        this.repository = repository;
        this.cloudinaryService = cloudinaryService;
    }

    public ProductCategory create(String name, MultipartFile image) throws IOException {
        String imageUrl = cloudinaryService.uploadImage(image);
        ProductCategory category = new ProductCategory();
        category.setName(name);
        category.setImageUrl(imageUrl);
        category.setStatus(true);
        return repository.save(category);
    }

    public List<ProductCategory> getAll() {
        return repository.findAll();
    }

    public List<ProductCategory> getActive() {
        return repository.findByStatusTrue();
    }

    public Optional<ProductCategory> getById(String id) {
        return repository.findById(id);
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    public ProductCategory enable(String id) {
        ProductCategory category = getById(id).orElseThrow(() -> new IllegalArgumentException("Category not found"));
        category.setStatus(true);
        return repository.save(category);
    }

    public ProductCategory disable(String id) {
        ProductCategory category = getById(id).orElseThrow(() -> new IllegalArgumentException("Category not found"));
        category.setStatus(false);
        return repository.save(category);
    }

    public ProductCategory update(String id, String name, MultipartFile image) throws IOException {
        ProductCategory category = getById(id).orElseThrow(() -> new IllegalArgumentException("Category not found"));
        category.setName(name);

        if (image != null && !image.isEmpty()) {
            String newImageUrl = cloudinaryService.uploadImage(image);
            category.setImageUrl(newImageUrl);
        } else if (image == null) {
            category.setImageUrl(null);
        }

        return repository.save(category);
    }
}
