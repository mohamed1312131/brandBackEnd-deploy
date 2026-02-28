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
                "cloud_name", "dkvgbsvbz",
                "api_key", "462672441514126",
                "api_secret", "tMklR2LUQ1OkVd5xhrDuQkXgKRQ",
                "secure", true
        ));
    }
}