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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "User authentication and profile management endpoints")
public class AuthController {
    
    private final UserSyncService userSyncService;
    
    /**
     * Get current authenticated user info
     */
    @Operation(
            summary = "Get current user profile",
            description = "Returns the authenticated user's profile information synchronized from Keycloak"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user profile"),
            @ApiResponse(responseCode = "401", description = "Not authenticated or invalid token")
    })
    @SecurityRequirement(name = "bearerAuth")
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
            response.put("isEmailVerified", user.getEmailVerified());
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
    @Operation(
            summary = "Get JWT token info",
            description = "Returns decoded information from the current JWT token. Useful for debugging authentication issues."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved token info"),
            @ApiResponse(responseCode = "401", description = "Not authenticated or invalid token")
    })
    @SecurityRequirement(name = "bearerAuth")
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
    @Operation(
            summary = "Health check",
            description = "Public endpoint to verify the auth service is running. No authentication required."
    )
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth service is running");
    }
}
