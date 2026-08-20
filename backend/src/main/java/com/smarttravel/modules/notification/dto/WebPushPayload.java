package com.smarttravel.modules.notification.dto;

import java.util.Map;

public record WebPushPayload(
        String title,
        String body,
        String icon,
        String badge,
        String url,
        String tag,
        String flightNumber,
        String eventType,
        Map<String, Object> data
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String title;
        private String body;
        private String icon = "/logo.png";
        private String badge = "/logo.png";
        private String url = "/tracked-flights";
        private String tag = "smarttravel-flight-alert";
        private String flightNumber;
        private String eventType;
        private Map<String, Object> data;

        public Builder title(String v) { this.title = v; return this; }
        public Builder body(String v) { this.body = v; return this; }
        public Builder icon(String v) { this.icon = v; return this; }
        public Builder badge(String v) { this.badge = v; return this; }
        public Builder url(String v) { this.url = v; return this; }
        public Builder tag(String v) { this.tag = v; return this; }
        public Builder flightNumber(String v) { this.flightNumber = v; return this; }
        public Builder eventType(String v) { this.eventType = v; return this; }
        public Builder data(Map<String, Object> v) { this.data = v; return this; }

        public WebPushPayload build() {
            return new WebPushPayload(title, body, icon, badge, url, tag, flightNumber, eventType, data);
        }
    }
}
