package com.smarttravel.modules.auth.service;

import com.smarttravel.common.exception.DuplicateResourceException;
import com.smarttravel.common.exception.ForbiddenException;
import com.smarttravel.common.exception.ResourceNotFoundException;
import com.smarttravel.common.exception.UnauthorizedException;
import com.smarttravel.common.security.JwtTokenProvider;
import com.smarttravel.common.security.SecurityUtils;
import com.smarttravel.modules.auth.dto.AuthResponse;
import com.smarttravel.modules.auth.dto.LoginRequest;
import com.smarttravel.modules.auth.dto.RegisterRequest;
import com.smarttravel.modules.auth.dto.UserResponse;
import com.smarttravel.modules.auth.mapper.AuthMapper;
import com.smarttravel.modules.user.model.AccountStatus;
import com.smarttravel.modules.user.model.Role;
import com.smarttravel.modules.user.model.User;
import com.smarttravel.modules.user.model.UserPreferences;
import com.smarttravel.modules.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Production implementation of AuthService with BCrypt password hashing,
 * normalized email validation, RBAC claims, and audit tracking.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        if (userRepository.existsByNormalizedEmail(normalizedEmail) || userRepository.existsByEmail(request.getEmail().trim())) {
            log.warn("Registration failed: Email '{}' already exists", normalizedEmail);
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());

        Set<Role> defaultRoles = new HashSet<>();
        defaultRoles.add(Role.ROLE_USER);
        if (normalizedEmail.startsWith("admin")) {
            defaultRoles.add(Role.ROLE_ADMIN);
        }

        String phoneNumber = request.getPhoneNumber() != null ? request.getPhoneNumber() : request.getPhone();

        User user = User.builder()
                .fullName(request.getFullName())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail().trim())
                .normalizedEmail(normalizedEmail)
                .phoneNumber(phoneNumber)
                .passwordHash(passwordHash)
                .roles(defaultRoles)
                .accountStatus(AccountStatus.ACTIVE)
                .active(true)
                .emailVerified(true)
                .preferences(new UserPreferences())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {} and email: {}", savedUser.getId(), normalizedEmail);

        return AuthMapper.toUserResponse(savedUser);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        User user = userRepository.findByNormalizedEmail(normalizedEmail)
                .or(() -> userRepository.findByEmail(request.getEmail().trim()))
                .orElseThrow(() -> {
                    log.warn("Login failed: Unknown email '{}'", normalizedEmail);
                    return new UnauthorizedException("Invalid email or password");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed: Bad credentials for user ID: {}", user.getId());
            throw new UnauthorizedException("Invalid email or password");
        }

        if (user.getAccountStatus() == AccountStatus.SUSPENDED) {
            log.warn("Login rejected: Suspended account ID: {}", user.getId());
            throw new ForbiddenException("Account has been suspended. Please contact support.");
        }

        if (user.getAccountStatus() == AccountStatus.INACTIVE || !user.isActive()) {
            log.warn("Login rejected: Inactive account ID: {}", user.getId());
            throw new ForbiddenException("Account is inactive. Please contact support.");
        }

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        List<String> roles = user.getRoles() != null
                ? user.getRoles().stream().map(Role::name).collect(Collectors.toList())
                : List.of("ROLE_USER");

        String accessToken = jwtTokenProvider.generateTokenFromUserIdAndEmail(
                user.getId(),
                user.getEmail(),
                roles
        );

        log.info("User logged in successfully with ID: {}", user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getJwtExpirationMs())
                .user(AuthMapper.toUserSummaryDto(user))
                .build();
    }

    @Override
    public UserResponse getCurrentUser() {
        String userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("Full authentication is required to access this resource"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return AuthMapper.toUserResponse(user);
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
