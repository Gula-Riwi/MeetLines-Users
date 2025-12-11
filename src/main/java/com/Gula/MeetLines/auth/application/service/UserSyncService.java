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
        
        log.info("Syncing user from Keycloak - ID: {}, Email: {}", keycloakUserId, email);
        
        // Try to find existing user by external provider ID
        Optional<AppUser> existingUser = appUserRepository.findByExternalProviderId(keycloakUserId);
        
        if (existingUser.isPresent()) {
            AppUser user = existingUser.get();
            // Update email and name if they changed
            if (!user.getEmail().equals(email)) {
                user.setEmail(email);
            }
            if (!user.getFullName().equals(name)) {
                user.setFullName(name);
            }
            return appUserRepository.save(user);
        }
        
        // Create new user
        AppUser newUser = AppUser.builder()
                .id(UUID.fromString(keycloakUserId))
                .email(email)
                .fullName(name != null ? name : email.split("@")[0])
                .authProvider("keycloak")
                .externalProviderId(keycloakUserId)
                .isEmailVerified(true)  // Keycloak has already verified the email
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
