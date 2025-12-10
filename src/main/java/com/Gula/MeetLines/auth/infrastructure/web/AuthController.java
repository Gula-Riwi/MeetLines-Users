package com.Gula.MeetLines.auth.infrastructure.web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Gula.MeetLines.auth.application.service.UserSyncService;
import com.Gula.MeetLines.auth.domain.entity.AppUser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final UserSyncService userSyncService;
    
    /**
     * Get current authenticated user info
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        try {
            AppUser user = userSyncService.syncAuthenticatedUser();
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("email", user.getEmail());
            response.put("fullName", user.getFullName());
            response.put("phone", user.getPhone());
            response.put("authProvider", user.getAuthProvider());
            response.put("isEmailVerified", user.getIsEmailVerified());
            response.put("createdAt", user.getCreatedAt());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting current user", e);
            return ResponseEntity.status(401).build();
        }
    }
    
    /**
     * Get JWT token info (for debugging)
     */
    @GetMapping("/token-info")
    public ResponseEntity<Map<String, Object>> getTokenInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            return ResponseEntity.status(401).build();
        }
        
        Jwt jwt = (Jwt) authentication.getPrincipal();
        
        Map<String, Object> response = new HashMap<>();
        response.put("sub", jwt.getClaimAsString("sub"));
        response.put("email", jwt.getClaimAsString("email"));
        response.put("name", jwt.getClaimAsString("name"));
        response.put("preferred_username", jwt.getClaimAsString("preferred_username"));
        response.put("given_name", jwt.getClaimAsString("given_name"));
        response.put("family_name", jwt.getClaimAsString("family_name"));
        response.put("email_verified", jwt.getClaimAsBoolean("email_verified"));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Health check endpoint (public)
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth service is running");
    }
}
