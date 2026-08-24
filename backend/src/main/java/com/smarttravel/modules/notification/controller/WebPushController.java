package com.smarttravel.modules.notification.controller;

import com.smarttravel.common.response.ApiResponse;
import com.smarttravel.common.security.SecurityUtils;
import com.smarttravel.modules.notification.dto.PushSubscriptionRequest;
import com.smarttravel.modules.notification.dto.WebPushPayload;
import com.smarttravel.modules.notification.model.PushSubscription;
import com.smarttravel.modules.notification.service.WebPushService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"/v1/notifications/push", "/api/v1/notifications/push"})
@Tag(name = "Web Push Notifications", description = "Browser Push Notification registration and subscriptions")
public class WebPushController {

    private final WebPushService webPushService;

    public WebPushController(WebPushService webPushService) {
        this.webPushService = webPushService;
    }

    @Operation(summary = "Get VAPID public key for browser push registration")
    @GetMapping("/public-key")
    public ResponseEntity<ApiResponse<Map<String, String>>> getVapidPublicKey() {
        String key = webPushService.getVapidPublicKey();
        return ResponseEntity.ok(ApiResponse.success("VAPID public key retrieved", Map.of("publicKey", key)));
    }

    @Operation(summary = "Register or update browser Web Push subscription")
    @PostMapping("/subscribe")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PushSubscription>> subscribe(
            @Valid @RequestBody PushSubscriptionRequest request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.smarttravel.common.security.UserPrincipal principal,
            Authentication authentication) {
        String userId = resolveUserId(principal, authentication);
        PushSubscription sub = webPushService.registerSubscription(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Push subscription registered", sub));
    }

    @Operation(summary = "Unsubscribe device from browser Web Push notifications")
    @PostMapping("/unsubscribe")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> unsubscribe(
            @RequestParam String endpoint,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.smarttravel.common.security.UserPrincipal principal,
            Authentication authentication) {
        String userId = resolveUserId(principal, authentication);
        webPushService.removeSubscription(userId, endpoint);
        return ResponseEntity.ok(ApiResponse.success("Push subscription removed"));
    }

    @Operation(summary = "Send a test push notification to user's registered devices")
    @PostMapping("/test")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> sendTestPush(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.smarttravel.common.security.UserPrincipal principal,
            Authentication authentication) {
        String userId = resolveUserId(principal, authentication);
        WebPushPayload payload = WebPushPayload.builder()
                .title("SmartTravel Notification")
                .body("Browser push notifications are active for your account!")
                .url("/tracked-flights")
                .tag("smarttravel-test-push")
                .build();
        webPushService.sendPushToUser(userId, payload);
        return ResponseEntity.ok(ApiResponse.success("Test push notification dispatched"));
    }

    private String resolveUserId(com.smarttravel.common.security.UserPrincipal principal, Authentication authentication) {
        if (principal != null && principal.getId() != null && !principal.getId().isBlank()) {
            return principal.getId();
        }
        if (authentication != null && authentication.getName() != null && !authentication.getName().isBlank() && !"anonymousUser".equals(authentication.getName())) {
            return authentication.getName();
        }
        return SecurityUtils.getCurrentUserId().orElseGet(SecurityUtils::getCurrentUsernameOrAnonymous);
    }
}

