package com.smarttravel.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload containing the Google Identity Services credential (ID Token).
 */
public class GoogleLoginRequest {

    @NotBlank(message = "Google ID token credential is required")
    private String credential;

    private boolean rememberMe = true;

    public GoogleLoginRequest() {
    }

    public GoogleLoginRequest(String credential, boolean rememberMe) {
        this.credential = credential;
        this.rememberMe = rememberMe;
    }

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}
