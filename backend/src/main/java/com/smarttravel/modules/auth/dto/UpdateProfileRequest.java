package com.smarttravel.modules.auth.dto;

import com.smarttravel.modules.user.model.UserPreferences;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Update User Profile & Travel Preferences Payload")
public class UpdateProfileRequest {

    @Size(max = 100, message = "Full name must be at most 100 characters")
    @Schema(description = "User's updated full name", example = "Sarah Connor")
    private String fullName;

    @Schema(description = "First name", example = "Sarah")
    private String firstName;

    @Schema(description = "Last name", example = "Connor")
    private String lastName;

    @Schema(description = "Contact phone number", example = "+91 98765 43210")
    private String phoneNumber;

    @Schema(description = "Travel, meal, and address preferences")
    private UserPreferences preferences;

    public UpdateProfileRequest() {
    }

    public UpdateProfileRequest(String fullName, String firstName, String lastName, String phoneNumber, UserPreferences preferences) {
        this.fullName = fullName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.preferences = preferences;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String fullName;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private UserPreferences preferences;

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

        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder preferences(UserPreferences preferences) {
            this.preferences = preferences;
            return this;
        }

        public UpdateProfileRequest build() {
            return new UpdateProfileRequest(fullName, firstName, lastName, phoneNumber, preferences);
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public UserPreferences getPreferences() {
        return preferences;
    }

    public void setPreferences(UserPreferences preferences) {
        this.preferences = preferences;
    }
}
