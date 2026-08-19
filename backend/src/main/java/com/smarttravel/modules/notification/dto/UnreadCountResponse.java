package com.smarttravel.modules.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Customer Unread Notifications Count")
public class UnreadCountResponse {

    @Schema(description = "Number of unread notifications", example = "3")
    private long unreadCount;

    public UnreadCountResponse() {
    }

    public UnreadCountResponse(long unreadCount) {
        this.unreadCount = unreadCount;
    }

    public long getUnreadCount() { return unreadCount; }
    public void setUnreadCount(long unreadCount) { this.unreadCount = unreadCount; }
}
