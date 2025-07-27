package com.example.hamzabackend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WebsiteInfoRequest {
    private String instagramUrl;
    private String facebookUrl;
    private String youtubeUrl;
    private String pinterestUrl;
    private String threadsUrl;
    private String phone;
    private String email;
    private String location;
    private String description;

    private AboutUs aboutUs;
    

    @Data
    @AllArgsConstructor
    public static class AboutUs {
        private String title;
        private String description;
    }
}