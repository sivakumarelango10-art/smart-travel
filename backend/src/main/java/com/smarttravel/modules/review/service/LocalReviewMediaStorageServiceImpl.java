package com.smarttravel.modules.review.service;

import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

/**
 * Local filesystem-backed production-safe implementation of ReviewMediaStorageService.
 * Enforces file type validation, size bounds, safe randomized naming, and path-traversal guards.
 */
@Service
public class LocalReviewMediaStorageServiceImpl implements ReviewMediaStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalReviewMediaStorageServiceImpl.class);

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );

    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB

    private final Path storageDirectory;

    public LocalReviewMediaStorageServiceImpl(
            @Value("${smarttravel.review.media-dir:./uploads/reviews}") String storagePath
    ) {
        this.storageDirectory = Paths.get(storagePath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storageDirectory);
            log.info("Initialized ReviewMediaStorage directory at: {}", this.storageDirectory);
        } catch (IOException e) {
            log.error("Could not initialize ReviewMediaStorage directory: {}", this.storageDirectory, e);
        }
    }

    @Override
    public String storePhoto(String reviewId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded review photo cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException("Uploaded review photo exceeds maximum limit of 5 MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Invalid photo format. Only JPG, PNG, and WebP images are allowed.");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename, contentType);

        // Clean review ID for safe file prefixing
        String sanitizedReviewId = (reviewId != null ? reviewId.replaceAll("[^a-zA-Z0-9_-]", "") : "general");
        String uniqueFilename = "rev_" + sanitizedReviewId + "_" + UUID.randomUUID() + extension;

        try {
            Path targetPath = this.storageDirectory.resolve(uniqueFilename).normalize();

            // Guard against path traversal attacks
            if (!targetPath.startsWith(this.storageDirectory)) {
                throw new BadRequestException("Invalid file path encountered during storage");
            }

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Stored photo for review ID '{}' as safe file '{}'", reviewId, uniqueFilename);

            return uniqueFilename;
        } catch (IOException e) {
            log.error("Failed to store review photo on disk: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to persist uploaded photo: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] loadPhoto(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new BadRequestException("Filename cannot be empty");
        }

        try {
            Path filePath = this.storageDirectory.resolve(filename).normalize();

            // Guard against directory traversal
            if (!filePath.startsWith(this.storageDirectory)) {
                throw new BadRequestException("Invalid photo request path");
            }

            if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
                throw new ResourceNotFoundException("Review photo not found: " + filename);
            }

            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("Failed to load review photo '{}': {}", filename, e.getMessage(), e);
            throw new RuntimeException("Could not read review photo: " + e.getMessage(), e);
        }
    }

    @Override
    public String getContentType(String filename) {
        if (filename == null || filename.isBlank()) {
            return "image/jpeg";
        }
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) {
            return "image/png";
        } else if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        return "image/jpeg";
    }

    @Override
    public void deletePhoto(String filename) {
        if (filename == null || filename.isBlank()) {
            return;
        }

        try {
            Path filePath = this.storageDirectory.resolve(filename).normalize();
            if (filePath.startsWith(this.storageDirectory)) {
                Files.deleteIfExists(filePath);
                log.info("Deleted review photo '{}'", filename);
            }
        } catch (IOException e) {
            log.warn("Could not delete review photo '{}': {}", filename, e.getMessage());
        }
    }

    private String getFileExtension(String originalFilename, String contentType) {
        if (originalFilename != null && originalFilename.contains(".")) {
            String ext = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            if (ext.equals(".jpg") || ext.equals(".jpeg") || ext.equals(".png") || ext.equals(".webp")) {
                return ext;
            }
        }

        return switch (contentType.toLowerCase()) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }
}
