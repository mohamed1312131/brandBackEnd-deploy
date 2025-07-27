package com.example.hamzabackend.service;

import com.example.hamzabackend.DTO.WebsiteInfoRequest;
import com.example.hamzabackend.entity.WebsiteInfo;
import com.example.hamzabackend.repository.WebsiteInfoRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    public WebsiteInfo updateInfo(WebsiteInfoRequest request, MultipartFile logo, MultipartFile aboutImage, List<MultipartFile> instagramImages) throws IOException {
        WebsiteInfo existing = getInfo();
        WebsiteInfo info = existing != null ? existing : new WebsiteInfo();

        // Set basic fields
        info.setInstagramUrl(request.getInstagramUrl());
        info.setFacebookUrl(request.getFacebookUrl());
        info.setYoutubeUrl(request.getYoutubeUrl());
        info.setPinterestUrl(request.getPinterestUrl());
        info.setThreadsUrl(request.getThreadsUrl());

        info.setPhone(request.getPhone());
        info.setEmail(request.getEmail());
        info.setLocation(request.getLocation());
        info.setDescription(request.getDescription());

        // About Us block
        if (info.getAboutUs() == null) {
            info.setAboutUs(new WebsiteInfo.AboutUs());
        }
        info.getAboutUs().setTitle(request.getAboutUs().getTitle());
        info.getAboutUs().setDescription(request.getAboutUs().getDescription());

        // Upload logo if provided
        if (logo != null && !logo.isEmpty()) {
            info.setLogoUrl(cloudinaryService.uploadFile(logo, "website", "image"));
        }

        // Upload About image if provided
        if (aboutImage != null && !aboutImage.isEmpty()) {
            info.getAboutUs().setImageUrl(cloudinaryService.uploadFile(aboutImage, "website", "image"));
        }

        // Upload Instagram images
        if (instagramImages != null && !instagramImages.isEmpty()) {
            List<String> urls = new ArrayList<>();
            for (MultipartFile file : instagramImages) {
                if (!file.isEmpty()) {
                    urls.add(cloudinaryService.uploadFile(file, "website/instagram", "image"));
                }
            }
            info.setInstagramUrls(urls);
        }

        return repository.save(info);
    }

    // Individual Getters
    public String getInstagramUrl() {
        WebsiteInfo info = getInfo();
        return info != null ? info.getInstagramUrl() : null;
    }

    public String getFacebookUrl() {
        WebsiteInfo info = getInfo();
        return info != null ? info.getFacebookUrl() : null;
    }

    public String getYoutubeUrl() {
        WebsiteInfo info = getInfo();
        return info != null ? info.getYoutubeUrl() : null;
    }

    public String getPinterestUrl() {
        WebsiteInfo info = getInfo();
        return info != null ? info.getPinterestUrl() : null;
    }

    public String getThreadsUrl() {
        WebsiteInfo info = getInfo();
        return info != null ? info.getThreadsUrl() : null;
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
