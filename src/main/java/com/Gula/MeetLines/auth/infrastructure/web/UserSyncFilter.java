package com.Gula.MeetLines.auth.infrastructure.web;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.Gula.MeetLines.auth.application.service.UserSyncService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserSyncFilter extends OncePerRequestFilter {
    
    private final UserSyncService userSyncService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // If user is authenticated and has a JWT token, sync them to the database
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            try {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                userSyncService.syncUserFromJwt(jwt);
                log.debug("User synced successfully for request: {}", request.getRequestURI());
            } catch (Exception e) {
                log.warn("Failed to sync user: {}", e.getMessage());
                // Don't fail the request if sync fails, just log it
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
