package com.smarttravel.modules.user.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * User MongoDB Document Entity representing travelers, admins, and staff identities.
 */
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    @Indexed(unique = true)
    private String normalizedEmail;

    @JsonIgnore
    private String passwordHash;

    private String fullName;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String phone;

    private Set<Role> roles = new HashSet<>();
    private AccountStatus accountStatus = AccountStatus.ACTIVE;
    private boolean active = true;
    private boolean emailVerified = true;

    private UserPreferences preferences = new UserPreferences();

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private Instant lastLoginAt;

    public User() {
    }

    public User(String id, String email, String normalizedEmail, String passwordHash, String fullName,
                String firstName, String lastName, String phoneNumber, String phone, Set<Role> roles,
                AccountStatus accountStatus, boolean active, boolean emailVerified,
                UserPreferences preferences, Instant createdAt, Instant updatedAt, Instant lastLoginAt) {
        this.id = id;
        this.email = email;
        this.normalizedEmail = normalizedEmail != null ? normalizedEmail : (email != null ? email.trim().toLowerCase(Locale.ROOT) : null);
        this.passwordHash = passwordHash;
        this.fullName = fullName != null ? fullName : computeFullName(firstName, lastName);
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber != null ? phoneNumber : phone;
        this.phone = this.phoneNumber;
        this.roles = roles != null ? roles : new HashSet<>();
        this.accountStatus = accountStatus != null ? accountStatus : (active ? AccountStatus.ACTIVE : AccountStatus.INACTIVE);
        this.active = this.accountStatus == AccountStatus.ACTIVE;
        this.emailVerified = emailVerified;
        this.preferences = preferences != null ? preferences : new UserPreferences();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastLoginAt = lastLoginAt;
    }

    private static String computeFullName(String firstName, String lastName) {
        if (firstName == null && lastName == null) return null;
        if (firstName == null) return lastName;
        if (lastName == null) return firstName;
        return (firstName + " " + lastName).trim();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String email;
        private String normalizedEmail;
        private String passwordHash;
        private String fullName;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private String phone;
        private Set<Role> roles = new HashSet<>();
        private AccountStatus accountStatus = AccountStatus.ACTIVE;
        private boolean active = true;
        private boolean emailVerified = true;
        private UserPreferences preferences = new UserPreferences();
        private Instant createdAt;
        private Instant updatedAt;
        private Instant lastLoginAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            if (this.normalizedEmail == null && email != null) {
                this.normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
            }
            return this;
        }

        public Builder normalizedEmail(String normalizedEmail) {
            this.normalizedEmail = normalizedEmail;
            return this;
        }

        public Builder passwordHash(String passwordHash) {
            this.passwordHash = passwordHash;
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

        public Builder roles(Set<Role> roles) {
            this.roles = roles;
            return this;
        }

        public Builder accountStatus(AccountStatus accountStatus) {
            this.accountStatus = accountStatus;
            this.active = (accountStatus == AccountStatus.ACTIVE);
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            this.accountStatus = active ? AccountStatus.ACTIVE : AccountStatus.INACTIVE;
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

        public User build() {
            return new User(id, email, normalizedEmail, passwordHash, fullName, firstName, lastName,
                    phoneNumber, phone, roles, accountStatus, active, emailVerified, preferences, createdAt, updatedAt, lastLoginAt);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        if (email != null) {
            this.normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        }
    }

    public String getNormalizedEmail() {
        return normalizedEmail;
    }

    public void setNormalizedEmail(String normalizedEmail) {
        this.normalizedEmail = normalizedEmail;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        if (fullName != null) {
            return fullName;
        }
        return computeFullName(firstName, lastName);
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

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
        this.active = (accountStatus == AccountStatus.ACTIVE);
    }

    public boolean isActive() {
        return accountStatus == AccountStatus.ACTIVE && active;
    }

    public void setActive(boolean active) {
        this.active = active;
        if (!active && this.accountStatus == AccountStatus.ACTIVE) {
            this.accountStatus = AccountStatus.INACTIVE;
        } else if (active) {
            this.accountStatus = AccountStatus.ACTIVE;
        }
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
