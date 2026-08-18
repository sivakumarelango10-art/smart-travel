package com.smarttravel.modules.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.smarttravel.modules.user.model.AccountStatus;
import com.smarttravel.modules.user.model.UserPreferences;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * Detailed User Profile Response for /api/auth/me and account management.
 * Strictly guarantees passwordHash is never exposed.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "User Profile Response Payload")
public class UserResponse {

    @Schema(description = "User Identifier", example = "66c1e101f1a2b3c4d5e6f701")
    private String id;

    @Schema(description = "Full Name", example = "John Doe")
    private String fullName;

    @Schema(description = "First Name", example = "John")
    private String firstName;

    @Schema(description = "Last Name", example = "Doe")
    private String lastName;

    @Schema(description = "Registered Email", example = "john.doe@smarttravel.com")
    private String email;

    @Schema(description = "Contact Phone Number", example = "+919876543210")
    private String phoneNumber;

    @Schema(description = "Assigned Roles", example = "[\"ROLE_USER\"]")
    private List<String> roles;

    @Schema(description = "Account Status", example = "ACTIVE")
    private AccountStatus accountStatus;

    @Schema(description = "Whether email is verified", example = "true")
    private boolean emailVerified;

    @Schema(description = "User Travel Preferences")
    private UserPreferences preferences;

    @Schema(description = "Account creation timestamp")
    private Instant createdAt;

    @Schema(description = "Account last update timestamp")
    private Instant updatedAt;

    @Schema(description = "Last login timestamp")
    private Instant lastLoginAt;

    public UserResponse() {
    }

    public UserResponse(String id, String fullName, String firstName, String lastName, String email,
                        String phoneNumber, List<String> roles, AccountStatus accountStatus,
                        boolean emailVerified, UserPreferences preferences, Instant createdAt,
                        Instant updatedAt, Instant lastLoginAt) {
        this.id = id;
        this.fullName = fullName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.roles = roles;
        this.accountStatus = accountStatus;
        this.emailVerified = emailVerified;
        this.preferences = preferences;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastLoginAt = lastLoginAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String fullName;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private List<String> roles;
        private AccountStatus accountStatus;
        private boolean emailVerified;
        private UserPreferences preferences;
        private Instant createdAt;
        private Instant updatedAt;
        private Instant lastLoginAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

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

        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder roles(List<String> roles) {
            this.roles = roles;
            return this;
        }

        public Builder accountStatus(AccountStatus accountStatus) {
            this.accountStatus = accountStatus;
            return this;
        }

        public Builder emailVerified(boolean emailVerified) {
            this.emailVerified = emailVerified;
            return this;
        }

        public Builder preferences(UserPreferences preferences) {
            this.preferences = preferences;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder lastLoginAt(Instant lastLoginAt) {
            this.lastLoginAt = lastLoginAt;
            return this;
        }

        public UserResponse build() {
            return new UserResponse(id, fullName, firstName, lastName, email, phoneNumber, roles,
                    accountStatus, emailVerified, preferences, createdAt, updatedAt, lastLoginAt);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public UserPreferences getPreferences() {
        return preferences;
    }

    public void setPreferences(UserPreferences preferences) {
        this.preferences = preferences;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
}
