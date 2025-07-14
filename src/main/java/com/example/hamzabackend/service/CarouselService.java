package com.example.hamzabackend.service;

import com.example.hamzabackend.entity.Carousel;
import com.example.hamzabackend.repository.CarouselRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class CarouselService {

    private final CarouselRepository carouselRepository;

    public CarouselService(CarouselRepository carouselRepository) {
        this.carouselRepository = carouselRepository;
    }

    public Carousel createCarousel(Carousel carousel) {
        carousel.setCreatedAt(Instant.now());
        carousel.setStatus(true);
        return carouselRepository.save(carousel);
    }

    public Carousel updateCarousel(String id, Carousel updatedCarousel) {
        return carouselRepository.findById(id)
                .map(existing -> {
                    existing.setTitle(updatedCarousel.getTitle());
                    existing.setDescription(updatedCarousel.getDescription());

                    if (updatedCarousel.getImageUrl() != null) {
                        existing.setImageUrl(
                                updatedCarousel.getImageUrl().isBlank() ? null : updatedCarousel.getImageUrl()
                        );
                    }

                    existing.setStatus(updatedCarousel.getStatus());
                    return carouselRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Carousel not found with ID: " + id));
    }



    public List<Carousel> getAllCarousels() {
        return carouselRepository.findAll();
    }

    public List<Carousel> getAllActiveCarousels() {
        return carouselRepository.findByStatusTrue();
    }

    public Optional<Carousel> getCarouselById(String id) {
        return carouselRepository.findById(id);
    }

    public void disableCarousel(String id) {
        carouselRepository.findById(id).ifPresent(carousel -> {
            carousel.setStatus(false);
            carouselRepository.save(carousel);
        });
    }

    public void deleteCarousel(String id) {
        carouselRepository.deleteById(id);
    }
    public void enableCarousel(String id) {
        carouselRepository.findById(id).ifPresent(carousel -> {
            carousel.setStatus(true);
            carouselRepository.save(carousel);
        });
    }

}
