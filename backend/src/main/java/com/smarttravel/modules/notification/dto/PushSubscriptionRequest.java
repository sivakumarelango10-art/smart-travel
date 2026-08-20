package com.smarttravel.modules.notification.dto;

import jakarta.validation.constraints.NotBlank;

public record PushSubscriptionRequest(
        @NotBlank(message = "Push endpoint is required")
        String endpoint,

        String p256dhKey,

        String authKey,

        String userAgent
) {}
