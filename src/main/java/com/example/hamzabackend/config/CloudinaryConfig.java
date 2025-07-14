package com.example.hamzabackend.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "electoro",
                "api_key", "395233879161639",
                "api_secret", "-58R9fG20HDhoRopSFnHdqmwXqo",
                "secure", true
        ));
    }
}