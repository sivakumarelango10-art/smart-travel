package com.smarttravel.modules.review.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Storage abstraction for review media and photo attachments.
 * Allows switching between local filesystem storage and cloud blob storage (e.g. AWS S3).
 */
public interface ReviewMediaStorageService {

    /**
     * Stores an uploaded photo for a specific review after validating file type and size.
     *
     * @param reviewId The ID of the review.
     * @param file The uploaded MultipartFile.
     * @return The safe public reference URL or filename.
     */
    String storePhoto(String reviewId, MultipartFile file);

    /**
     * Loads the raw byte content of a stored photo by filename.
     *
     * @param filename The sanitized filename.
     * @return The photo byte array.
     */
    byte[] loadPhoto(String filename);

    /**
     * Resolves the MIME content type from the stored photo filename.
     *
     * @param filename The sanitized filename.
     * @return The MIME string (e.g., image/jpeg).
     */
    String getContentType(String filename);

    /**
     * Deletes a previously stored review photo.
     *
     * @param filename The sanitized filename.
     */
    void deletePhoto(String filename);
}
