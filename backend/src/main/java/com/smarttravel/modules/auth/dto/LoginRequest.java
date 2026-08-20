package com.smarttravel.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Authentication Login Request Payload")
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "User registered email", example = "traveler@smarttravel.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(description = "User password", example = "Travel2026!Secure", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "Extend session expiration for trusted devices (Remember Me)", example = "true")
    private Boolean rememberMe = false;

    public LoginRequest() {
    }

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
        this.rememberMe = false;
    }

    public LoginRequest(String email, String password, Boolean rememberMe) {
        this.email = email;
        this.password = password;
        this.rememberMe = rememberMe != null ? rememberMe : false;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String email;
        private String password;
        private Boolean rememberMe = false;

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder rememberMe(Boolean rememberMe) {
            this.rememberMe = rememberMe;
            return this;
        }

        public LoginRequest build() {
            return new LoginRequest(email, password, rememberMe);
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getRememberMe() {
        return rememberMe;
    }

    public boolean isRememberMe() {
        return rememberMe != null && rememberMe;
    }

    public void setRememberMe(Boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}
