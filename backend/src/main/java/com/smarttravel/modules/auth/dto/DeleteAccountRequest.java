package com.smarttravel.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Account Deletion Confirmation Request")
public class DeleteAccountRequest {

    @Schema(description = "User's current password for verification (optional depending on auth session)", example = "Travel2026!Secure")
    private String password;

    @Schema(description = "Reason for account deletion", example = "No longer using service")
    private String reason;

    public DeleteAccountRequest() {
    }

    public DeleteAccountRequest(String password, String reason) {
        this.password = password;
        this.reason = reason;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String password;
        private String reason;

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public DeleteAccountRequest build() {
            return new DeleteAccountRequest(password, reason);
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
