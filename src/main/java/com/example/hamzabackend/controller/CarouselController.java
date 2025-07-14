package com.example.hamzabackend.controller;

import com.example.hamzabackend.entity.Carousel;
import com.example.hamzabackend.service.CarouselService;
import com.example.hamzabackend.service.CloudinaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/carousels")
public class CarouselController {

    private final CarouselService carouselService;
    private final CloudinaryService cloudinaryService;

    public CarouselController(CarouselService carouselService, CloudinaryService cloudinaryService) {
        this.carouselService = carouselService;
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping
    public ResponseEntity<Carousel> createCarousel(@RequestParam("image") MultipartFile image,
                                                   @RequestParam("title") String title,
                                                   @RequestParam("description") String description) throws IOException {
        String imageUrl = cloudinaryService.uploadImage(image);
        Carousel carousel = new Carousel();
        carousel.setImageUrl(imageUrl);
        carousel.setTitle(title);
        carousel.setDescription(description);
        return ResponseEntity.ok(carouselService.createCarousel(carousel));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Carousel> updateCarousel(@PathVariable String id,
                                                   @RequestParam(value = "image", required = false) MultipartFile image,
                                                   @RequestParam(value = "imageRemoved", required = false) String imageRemoved,
                                                   @RequestParam("title") String title,
                                                   @RequestParam("description") String description,
                                                   @RequestParam("status") String status) throws IOException {
        Carousel updated = new Carousel();
        updated.setTitle(title);
        updated.setDescription(description);
        updated.setStatus(Boolean.parseBoolean(status));

        if (image != null && !image.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(image);
            updated.setImageUrl(imageUrl);
        } else if ("true".equalsIgnoreCase(imageRemoved)) {
            updated.setImageUrl(""); // 🔥 only clear image if explicitly told
        }
        // Else → do not set imageUrl at all → backend will keep existing one

        return ResponseEntity.ok(carouselService.updateCarousel(id, updated));
    }


    @GetMapping
    public List<Carousel> getAllCarousels() {
        return carouselService.getAllCarousels();
    }

    @GetMapping("/active")
    public List<Carousel> getAllActiveCarousels() {
        return carouselService.getAllActiveCarousels();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Carousel> getCarouselById(@PathVariable String id) {
        return carouselService.getCarouselById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/disable")
    public ResponseEntity<Void> disableCarousel(@PathVariable String id) {
        carouselService.disableCarousel(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCarousel(@PathVariable String id) {
        carouselService.deleteCarousel(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{id}/enable")
    public ResponseEntity<Void> enableCarousel(@PathVariable String id) {
        carouselService.enableCarousel(id);
        return ResponseEntity.noContent().build();
    }
}
