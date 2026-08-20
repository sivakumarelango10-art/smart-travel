package com.smarttravel.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "User Registration Request Payload")
public class RegisterRequest {

    @Schema(description = "Full Name", example = "John Doe")
    private String fullName;

    @Schema(description = "First Name", example = "John")
    private String firstName;

    @Schema(description = "Last Name", example = "Doe")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Unique email address", example = "john.doe@smarttravel.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters long")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!._-]).*$",
            message = "Password must contain at least one digit, one lowercase, one uppercase letter, and one special character (@#$%^&+=!._-)"
    )
    @Schema(description = "Strong password", example = "Travel2026!Secure", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "Password confirmation", example = "Travel2026!Secure")
    private String confirmPassword;

    @Schema(description = "Contact phone number", example = "+919876543210")
    private String phoneNumber;

    @Schema(description = "Contact phone (alias)", example = "+919876543210")
    private String phone;

    public RegisterRequest() {
    }

    public RegisterRequest(String fullName, String email, String password, String phoneNumber) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.confirmPassword = password;
        this.phoneNumber = phoneNumber;
        this.phone = phoneNumber;
    }

    public RegisterRequest(String fullName, String email, String password, String confirmPassword, String phoneNumber) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.phoneNumber = phoneNumber;
        this.phone = phoneNumber;
    }

    public RegisterRequest(String firstName, String lastName, String email, String password, String phone, String fullName, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber != null ? phoneNumber : phone;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String fullName;
        private String firstName;
        private String lastName;
        private String email;
        private String password;
        private String confirmPassword;
        private String phoneNumber;
        private String phone;

        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder confirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
            return this;
        }

        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            this.phone = phoneNumber;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            this.phoneNumber = phone;
            return this;
        }

        public RegisterRequest build() {
            RegisterRequest req = new RegisterRequest(firstName, lastName, email, password, phone, fullName, phoneNumber);
            req.setConfirmPassword(confirmPassword != null ? confirmPassword : password);
            return req;
        }
    }

    public String getFullName() {
        if (fullName != null && !fullName.isBlank()) {
            return fullName;
        }
        if (firstName != null || lastName != null) {
            String f = firstName != null ? firstName : "";
            String l = lastName != null ? lastName : "";
            return (f + " " + l).trim();
        }
        return null;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getPhoneNumber() {
        return phoneNumber != null ? phoneNumber : phone;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        this.phone = phoneNumber;
    }

    public String getPhone() {
        return phone != null ? phone : phoneNumber;
    }

    public void setPhone(String phone) {
        this.phone = phone;
        this.phoneNumber = phone;
    }
}
