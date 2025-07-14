package com.example.hamzabackend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;
    private static final Logger logger = LoggerFactory.getLogger(CloudinaryService.class);

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadFile(MultipartFile file, String folderName, String resourceType) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File to upload cannot be null or empty");
        }

        if (!"auto".equals(resourceType) && !"image".equals(resourceType) && !"video".equals(resourceType)) {
            throw new IllegalArgumentException("Invalid resource_type specified. Must be 'auto', 'image', or 'video'.");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new IllegalArgumentException("File content type cannot be determined.");
        }

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", resourceType,
                            "folder", "planandexercise/" + folderName,
                            "public_id", generatePublicId(file.getOriginalFilename()),
                            "overwrite", false,
                            "unique_filename", true
                    )
            );
            String secureUrl = (String) uploadResult.get("secure_url");
            logger.info("File uploaded successfully to Cloudinary. URL: {}", secureUrl);
            return secureUrl;
        } catch (IOException e) {
            logger.error("Failed to upload file {} to Cloudinary folder {}", file.getOriginalFilename(), folderName, e);
            throw new IOException("Failed to upload file to Cloudinary: " + file.getOriginalFilename(), e);
        } catch (Exception e) {
            logger.error("Cloudinary upload failed for file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Cloudinary upload failed for file: " + file.getOriginalFilename(), e);
        }
    }

    private String generatePublicId(String originalFilename) {
        // Remove file extension if present
        String nameWithoutExt = originalFilename.lastIndexOf('.') > 0
                ? originalFilename.substring(0, originalFilename.lastIndexOf('.'))
                : originalFilename;

        // Replace all non-alphanumeric characters (except underscores) with underscores
        // Also replace spaces with underscores
        String sanitized = nameWithoutExt.replaceAll("[^a-zA-Z0-9_]", "_")
                .replaceAll("\\s+", "_");

        // Add UUID and limit length to avoid hitting Cloudinary's 255-char limit
        return sanitized.length() > 100
                ? sanitized.substring(0, 100) + "_" + UUID.randomUUID()
                : sanitized + "_" + UUID.randomUUID();
    }

    public String uploadImage(MultipartFile imageFile) throws IOException {
        return uploadFile(imageFile, "exercise_images", "image");
    }

    public String uploadVideo(MultipartFile videoFile) throws IOException {
        return uploadFile(videoFile, "exercise_videos", "video");
    }
}