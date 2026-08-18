package com.smarttravel.modules.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Authentication Success Response Payload")
public class AuthResponse {

    @Schema(description = "JWT Access Token")
    private String accessToken;

    @Schema(description = "Token Type", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Token expiration in milliseconds", example = "86400000")
    private long expiresIn;

    @Schema(description = "Authenticated user details")
    private UserSummaryDto user;

    @Schema(description = "User Identifier (deprecated, see user.id)", example = "66c1e101f1a2b3c4d5e6f701")
    private String userId;

    @Schema(description = "User Email (deprecated, see user.email)", example = "john.doe@smarttravel.com")
    private String email;

    @Schema(description = "User Full Name", example = "John Doe")
    private String fullName;

    @Schema(description = "User First Name")
    private String firstName;

    @Schema(description = "User Last Name")
    private String lastName;

    @Schema(description = "Assigned Roles", example = "[\"ROLE_USER\"]")
    private List<String> roles;

    public AuthResponse() {
    }

    public AuthResponse(String accessToken, String tokenType, long expiresIn, UserSummaryDto user) {
        this.accessToken = accessToken;
        this.tokenType = tokenType != null ? tokenType : "Bearer";
        this.expiresIn = expiresIn;
        this.user = user;
        if (user != null) {
            this.userId = user.getId();
            this.email = user.getEmail();
            this.fullName = user.getFullName();
            this.roles = user.getRoles();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String accessToken;
        private String tokenType = "Bearer";
        private long expiresIn;
        private UserSummaryDto user;
        private String userId;
        private String email;
        private String fullName;
        private String firstName;
        private String lastName;
        private List<String> roles;

        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder tokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public Builder expiresIn(long expiresIn) {
            this.expiresIn = expiresIn;
            return this;
        }

        public Builder user(UserSummaryDto user) {
            this.user = user;
            if (user != null) {
                this.userId = user.getId();
                this.email = user.getEmail();
                this.fullName = user.getFullName();
                this.roles = user.getRoles();
            }
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
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

        public Builder roles(List<String> roles) {
            this.roles = roles;
            return this;
        }

        public AuthResponse build() {
            AuthResponse response = new AuthResponse(accessToken, tokenType, expiresIn, user);
            if (response.getUser() == null && (userId != null || email != null || fullName != null || roles != null)) {
                response.setUser(new UserSummaryDto(userId, fullName, email, roles));
            }
            if (firstName != null) response.setFirstName(firstName);
            if (lastName != null) response.setLastName(lastName);
            return response;
        }
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public UserSummaryDto getUser() {
        return user;
    }

    public void setUser(UserSummaryDto user) {
        this.user = user;
        if (user != null) {
            this.userId = user.getId();
            this.email = user.getEmail();
            this.fullName = user.getFullName();
            this.roles = user.getRoles();
        }
    }

    public String getUserId() {
        return userId != null ? userId : (user != null ? user.getId() : null);
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email != null ? email : (user != null ? user.getEmail() : null);
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName != null ? fullName : (user != null ? user.getFullName() : null);
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

    public List<String> getRoles() {
        return roles != null ? roles : (user != null ? user.getRoles() : null);
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
