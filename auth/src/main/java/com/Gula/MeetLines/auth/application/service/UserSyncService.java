package com.Gula.MeetLines.auth.application.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.Gula.MeetLines.auth.domain.entity.AppUser;
import com.Gula.MeetLines.auth.domain.repository.AppUserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSyncService {
    
    private final AppUserRepository appUserRepository;
    
    /**
     * Get or create an AppUser from the current authenticated user
     */
    public AppUser syncAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            throw new RuntimeException("User is not authenticated");
        }
        
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return syncUserFromJwt(jwt);
    }
    
    /**
     * Sync or create a user from JWT token claims
     */
    public AppUser syncUserFromJwt(Jwt jwt) {
        String keycloakUserId = jwt.getClaimAsString("sub");
        String email = jwt.getClaimAsString("email");
        String name = jwt.getClaimAsString("name");
        
        // Validate required fields
        if (keycloakUserId == null || keycloakUserId.isBlank()) {
            throw new IllegalArgumentException("JWT 'sub' claim (user ID) is required");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("JWT 'email' claim is required");
        }
        
        log.info("Syncing user from Keycloak - ID: {}, Email: {}", keycloakUserId, email);
        
        // Try to find existing user by external provider ID
        Optional<AppUser> existingUser = appUserRepository.findByExternalProviderId(keycloakUserId);
        
        if (existingUser.isPresent()) {
            AppUser user = existingUser.get();
            // Update email and name if they changed
            if (email != null && !email.equals(user.getEmail())) {
                user.setEmail(email);
            }
            if (name != null && !name.equals(user.getFullName())) {
                user.setFullName(name);
            }
            return appUserRepository.save(user);
        }
        
        // Create new user
        String displayName = name;
        if (displayName == null || displayName.isBlank()) {
            displayName = (email != null && email.contains("@")) 
                ? email.substring(0, email.indexOf("@")) 
                : "User";
        }
        
        AppUser newUser = AppUser.builder()
                .id(UUID.fromString(keycloakUserId))
                .email(email)
                .fullName(displayName)
                .authProvider("keycloak")
                .externalProviderId(keycloakUserId)
                .emailVerified(true)  // Keycloak has already verified the email
                .build();
        
        AppUser savedUser = appUserRepository.save(newUser);
        log.info("Created new AppUser - ID: {}, Email: {}", savedUser.getId(), savedUser.getEmail());
        
        return savedUser;
    }
    
    /**
     * Get user by ID
     */
    public Optional<AppUser> getUserById(UUID userId) {
        return appUserRepository.findById(userId);
    }
    
    /**
     * Get user by email
     */
    public Optional<AppUser> getUserByEmail(String email) {
        return appUserRepository.findByEmail(email);
    }
    
    /**
     * Get user by Keycloak external ID
     */
    public Optional<AppUser> getUserByKeycloakId(String keycloakId) {
        return appUserRepository.findByExternalProviderId(keycloakId);
    }
}
