package com.example.hamzabackend.service;


import com.example.hamzabackend.DTO.WebsiteInfoRequest;
import com.example.hamzabackend.entity.WebsiteInfo;
import com.example.hamzabackend.repository.WebsiteInfoRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class WebsiteInfoService {

    private final WebsiteInfoRepository repository;
    private final CloudinaryService cloudinaryService;

    public WebsiteInfoService(WebsiteInfoRepository repository, CloudinaryService cloudinaryService) {
        this.repository = repository;
        this.cloudinaryService = cloudinaryService;
    }

    public WebsiteInfo getInfo() {
        return repository.findAll().stream().findFirst().orElse(null);
    }

    public WebsiteInfo updateInfo(WebsiteInfoRequest request, MultipartFile logo, MultipartFile aboutImage) throws IOException {
        WebsiteInfo existing = getInfo();
        WebsiteInfo info = existing != null ? existing : new WebsiteInfo();

        // Set text fields
        info.setInstagramUrl(request.getInstagramUrl());
        info.setPhone(request.getPhone());
        info.setEmail(request.getEmail());
        info.setLocation(request.getLocation());
        info.setDescription(request.getDescription());

        if (info.getAboutUs() == null) {
            info.setAboutUs(new WebsiteInfo.AboutUs());
        }

        info.getAboutUs().setTitle(request.getAboutUs().getTitle());
        info.getAboutUs().setDescription(request.getAboutUs().getDescription());

        // Handle images
        if (logo != null && !logo.isEmpty()) {
            info.setLogoUrl(cloudinaryService.uploadFile(logo, "website", "image"));
        }

        if (aboutImage != null && !aboutImage.isEmpty()) {
            info.getAboutUs().setImageUrl(cloudinaryService.uploadFile(aboutImage, "website", "image"));
        }

        return repository.save(info);
    }

    // Individual Getters
    public String getInstagramUrl() {
        WebsiteInfo info = getInfo();
        return info != null ? info.getInstagramUrl() : null;
    }

    public String getPhone() {
        WebsiteInfo info = getInfo();
        return info != null ? info.getPhone() : null;
    }

    public String getEmail() {
        WebsiteInfo info = getInfo();
        return info != null ? info.getEmail() : null;
    }

    public String getLocation() {
        WebsiteInfo info = getInfo();
        return info != null ? info.getLocation() : null;
    }

    public String getDescription() {
        WebsiteInfo info = getInfo();
        return info != null ? info.getDescription() : null;
    }

    public String getLogoUrl() {
        WebsiteInfo info = getInfo();
        return info != null ? info.getLogoUrl() : null;
    }

    public WebsiteInfo.AboutUs getAboutUs() {
        WebsiteInfo info = getInfo();
        return info != null ? info.getAboutUs() : null;
    }
}
