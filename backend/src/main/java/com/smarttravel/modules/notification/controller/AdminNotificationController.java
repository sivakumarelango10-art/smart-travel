package com.smarttravel.modules.notification.controller;

import com.smarttravel.common.response.ApiResponse;
import com.smarttravel.modules.notification.dto.NotificationResponse;
import com.smarttravel.modules.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Administrative controller for retrying and inspecting customer communications.
 */
@RestController
@RequestMapping("/api/v1/admin/notifications")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Notification Operations", description = "Privileged notification management and retry endpoints")
@SecurityRequirement(name = "BearerAuth")
public class AdminNotificationController {

    private final NotificationService notificationService;

    public AdminNotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/{id}/retry")
    @Operation(summary = "Retry Failed Notification", description = "Retries outbound dispatch for a failed notification (bounded to max retries)")
    public ResponseEntity<ApiResponse<NotificationResponse>> retryNotification(@PathVariable String id) {
        NotificationResponse response = notificationService.retryNotification(id);
        return ResponseEntity.ok(ApiResponse.success("Notification retry executed", response));
    }
}
