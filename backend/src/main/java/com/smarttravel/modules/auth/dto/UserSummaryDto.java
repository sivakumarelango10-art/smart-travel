package com.smarttravel.modules.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Lightweight user summary payload included inside auth tokens/responses.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Authenticated User Summary")
public class UserSummaryDto {

    @Schema(description = "User Unique Identifier", example = "66c1e101f1a2b3c4d5e6f701")
    private String id;

    @Schema(description = "User Full Name", example = "John Doe")
    private String fullName;

    @Schema(description = "User Registered Email", example = "john.doe@smarttravel.com")
    private String email;

    @Schema(description = "Assigned Roles", example = "[\"ROLE_USER\"]")
    private List<String> roles;

    @Schema(description = "Account Status", example = "ACTIVE")
    private String accountStatus;

    public UserSummaryDto() {
    }

    public UserSummaryDto(String id, String fullName, String email, List<String> roles) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.roles = roles;
    }

    public UserSummaryDto(String id, String fullName, String email, List<String> roles, String accountStatus) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.roles = roles;
        this.accountStatus = accountStatus;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String fullName;
        private String email;
        private List<String> roles;
        private String accountStatus;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder roles(List<String> roles) {
            this.roles = roles;
            return this;
        }

        public Builder accountStatus(String accountStatus) {
            this.accountStatus = accountStatus;
            return this;
        }

        public UserSummaryDto build() {
            return new UserSummaryDto(id, fullName, email, roles, accountStatus);
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }
}
