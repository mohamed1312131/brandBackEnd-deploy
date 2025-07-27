package com.example.hamzabackend.controller;


import com.example.hamzabackend.DTO.WebsiteInfoRequest;
import com.example.hamzabackend.entity.WebsiteInfo;
import com.example.hamzabackend.service.WebsiteInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestPart;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/website")
// CORS is handled globally in SecurityConfig
public class WebsiteInfoController {

    private final WebsiteInfoService service;

    public WebsiteInfoController(WebsiteInfoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<WebsiteInfo> getInfo() {
        WebsiteInfo info = service.getInfo();
        return info != null ? ResponseEntity.ok(info) : ResponseEntity.notFound().build();
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<WebsiteInfo> updateInfo(
            @RequestPart("info") WebsiteInfoRequest info,
            @RequestPart(value = "logo", required = false) MultipartFile logo,
            @RequestPart(value = "aboutImage", required = false) MultipartFile aboutImage,
            @RequestPart(value = "instagramImages", required = false) List<MultipartFile> instagramImages
    ) {
        try {
            WebsiteInfo updated = service.updateInfo(info, logo, aboutImage, instagramImages);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/instagram")
    public ResponseEntity<String> getInstagramUrl() {
        return ResponseEntity.ok(service.getInstagramUrl());
    }

    @GetMapping("/phone")
    public ResponseEntity<String> getPhone() {
        return ResponseEntity.ok(service.getPhone());
    }

    @GetMapping("/email")
    public ResponseEntity<String> getEmail() {
        return ResponseEntity.ok(service.getEmail());
    }

    @GetMapping("/location")
    public ResponseEntity<String> getLocation() {
        return ResponseEntity.ok(service.getLocation());
    }

    @GetMapping("/description")
    public ResponseEntity<String> getDescription() {
        return ResponseEntity.ok(service.getDescription());
    }

    @GetMapping("/logo")
    public ResponseEntity<String> getLogoUrl() {
        return ResponseEntity.ok(service.getLogoUrl());
    }

    @GetMapping("/about")
    public ResponseEntity<WebsiteInfo.AboutUs> getAboutUs() {
        return ResponseEntity.ok(service.getAboutUs());
    }
    @GetMapping("/instagram-images")
    public ResponseEntity<List<String>> getInstagramImages() {
        WebsiteInfo info = service.getInfo();
        return ResponseEntity.ok(info != null ? info.getInstagramUrls() : new ArrayList<>());
    }
    @GetMapping("/facebook")
public ResponseEntity<String> getFacebookUrl() {
    return ResponseEntity.ok(service.getInfo().getFacebookUrl());
}

@GetMapping("/youtube")
public ResponseEntity<String> getYoutubeUrl() {
    return ResponseEntity.ok(service.getInfo().getYoutubeUrl());
}

@GetMapping("/pinterest")
public ResponseEntity<String> getPinterestUrl() {
    return ResponseEntity.ok(service.getInfo().getPinterestUrl());
}

@GetMapping("/threads")
public ResponseEntity<String> getThreadsUrl() {
    return ResponseEntity.ok(service.getInfo().getThreadsUrl());
}

}
