package com.smarttravel.common.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smarttravel.modules.user.model.AccountStatus;
import com.smarttravel.modules.user.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring Security UserDetails representation of authenticated principal.
 */
public class UserPrincipal implements UserDetails {

    private String id;
    private String email;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;
    private boolean active;
    private boolean accountNonLocked;

    public UserPrincipal() {
    }

    public UserPrincipal(String id, String email, String password, Collection<? extends GrantedAuthority> authorities, boolean active) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.active = active;
        this.accountNonLocked = active;
    }

    public UserPrincipal(String id, String email, String password, Collection<? extends GrantedAuthority> authorities, boolean active, boolean accountNonLocked) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.active = active;
        this.accountNonLocked = accountNonLocked;
    }

    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = user.getRoles() != null
                ? user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(role.name()))
                    .collect(Collectors.toList())
                : List.of();

        boolean isNonLocked = user.getAccountStatus() != AccountStatus.SUSPENDED;
        boolean isEnabled = user.getAccountStatus() == AccountStatus.ACTIVE && user.isActive();

        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                authorities,
                isEnabled,
                isNonLocked
        );
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
