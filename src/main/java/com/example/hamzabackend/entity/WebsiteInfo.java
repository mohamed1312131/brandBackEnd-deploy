package com.example.hamzabackend.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;



import lombok.*;

import java.util.List;


@Document(collection = "website_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebsiteInfo {
    @Id
    private String id;

    private String instagramUrl;
    private String phone;
    private String email;
    private String location;
    private String description;

    private AboutUs aboutUs;
    private String logoUrl;
    private List<String> instagramUrls;
    private String facebookUrl;
private String youtubeUrl;
private String pinterestUrl;
private String threadsUrl;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AboutUs {
        private String title;
        private String description;
        private String imageUrl;
    }
}
