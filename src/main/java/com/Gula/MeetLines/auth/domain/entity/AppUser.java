package com.Gula.MeetLines.auth.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "app_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {
    
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;
    
    @Column(nullable = false, unique = true, length = 150)
    private String email;
    
    @Column(length = 255)
    private String passwordHash;
    
    @Column(nullable = false, length = 150)
    private String fullName;
    
    @Column(length = 50)
    private String phone;
    
    @Column(columnDefinition = "boolean default false")
    private Boolean isEmailVerified;
    
    @Column(columnDefinition = "boolean default false")
    private Boolean isPhoneVerified;
    
    @Column(length = 50)
    private String authProvider;
    
    @Column(length = 255)
    private String externalProviderId;
    
    @Column(columnDefinition = "timestamp with time zone default CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    
    @Column(columnDefinition = "timestamp with time zone default CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (isEmailVerified == null) {
            isEmailVerified = false;
        }
        if (isPhoneVerified == null) {
            isPhoneVerified = false;
        }
        if (authProvider == null) {
            authProvider = "keycloak";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
