package com.smarttravel.modules.review.service.storage;

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

    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );

    private final Path storageDirectory;

    public LocalReviewMediaStorageServiceImpl(
            @Value("${smarttravel.storage.reviews-dir:uploads/reviews}") String storageDir) {
        this.storageDirectory = Paths.get(storageDir).toAbsolutePath().normalize();
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
            throw new BadRequestException("Uploaded photo file cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException("Photo file size exceeds maximum limit of 5 MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Invalid photo format '" + contentType + "'. Allowed formats: JPEG, PNG, WebP");
        }

        String extension = resolveExtension(file.getOriginalFilename(), contentType);
        String safeFilename = "rev_" + sanitize(reviewId) + "_" + UUID.randomUUID() + extension;

        try {
            Path targetLocation = storageDirectory.resolve(safeFilename).normalize();
            // Path traversal guard
            if (!targetLocation.startsWith(storageDirectory)) {
                throw new BadRequestException("Invalid filename security error");
            }

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("Stored photo for review ID '{}' as safe file '{}'", reviewId, safeFilename);

            // Return relative public serving endpoint URL
            return "/api/v1/reviews/photos/" + safeFilename;
        } catch (IOException e) {
            log.error("Failed to store uploaded review photo", e);
            throw new RuntimeException("Failed to persist photo file", e);
        }
    }

    @Override
    public byte[] loadPhoto(String filename) {
        if (filename == null || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new BadRequestException("Invalid photo filename parameter");
        }

        Path filePath = storageDirectory.resolve(filename).normalize();
        if (!filePath.startsWith(storageDirectory) || !Files.exists(filePath)) {
            throw new ResourceNotFoundException("ReviewPhoto", "filename", filename);
        }

        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("Failed to read photo file '{}'", filename, e);
            throw new RuntimeException("Could not read photo file", e);
        }
    }

    @Override
    public String getContentType(String filename) {
        if (filename == null) return "application/octet-stream";
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
    }

    @Override
    public void deletePhoto(String filename) {
        if (filename == null || filename.contains("..")) return;
        try {
            Path filePath = storageDirectory.resolve(filename).normalize();
            if (filePath.startsWith(storageDirectory)) {
                Files.deleteIfExists(filePath);
                log.info("Deleted photo file '{}'", filename);
            }
        } catch (IOException e) {
            log.warn("Could not delete photo file '{}'", filename, e);
        }
    }

    private String resolveExtension(String originalFilename, String contentType) {
        if (originalFilename != null && originalFilename.lastIndexOf('.') > 0) {
            String ext = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
            if (ext.equals(".jpg") || ext.equals(".jpeg") || ext.equals(".png") || ext.equals(".webp")) {
                return ext;
            }
        }
        if ("image/png".equalsIgnoreCase(contentType)) return ".png";
        if ("image/webp".equalsIgnoreCase(contentType)) return ".webp";
        return ".jpg";
    }

    private String sanitize(String input) {
        if (input == null) return "na";
        return input.replaceAll("[^a-zA-Z0-9_-]", "");
    }
}
