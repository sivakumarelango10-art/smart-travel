package com.smarttravel.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Change Password Request Payload")
public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required")
    @Schema(description = "Existing password", example = "OldPass123!Secure", requiredMode = Schema.RequiredMode.REQUIRED)
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters long")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!._-]).*$",
            message = "Password must contain at least one digit, one lowercase, one uppercase letter, and one special character (@#$%^&+=!._-)"
    )
    @Schema(description = "Strong new password", example = "NewPass2026!Secure", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;

    @Schema(description = "Confirmation of new password", example = "NewPass2026!Secure")
    private String confirmPassword;

    public ChangePasswordRequest() {
    }

    public ChangePasswordRequest(String currentPassword, String newPassword, String confirmPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String currentPassword;
        private String newPassword;
        private String confirmPassword;

        public Builder currentPassword(String currentPassword) {
            this.currentPassword = currentPassword;
            return this;
        }

        public Builder newPassword(String newPassword) {
            this.newPassword = newPassword;
            return this;
        }

        public Builder confirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
            return this;
        }

        public ChangePasswordRequest build() {
            return new ChangePasswordRequest(currentPassword, newPassword, confirmPassword);
        }
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
