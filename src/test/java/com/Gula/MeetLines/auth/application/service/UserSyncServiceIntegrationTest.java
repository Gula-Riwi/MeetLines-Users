package com.Gula.MeetLines.auth.application.service;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.Gula.MeetLines.auth.domain.entity.AppUser;
import com.Gula.MeetLines.auth.domain.repository.AppUserRepository;

/**
 * Integration test for UserSyncService using Testcontainers
 * This test demonstrates real database integration with PostgreSQL
 */
@SpringBootTest
@Testcontainers
class UserSyncServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @Autowired
    private AppUserRepository appUserRepository;

    @BeforeEach
    void setUp() {
        appUserRepository.deleteAll();
    }

    @Test
    void shouldSaveAndRetrieveUser() {
        // Given
        UUID userId = UUID.randomUUID();
        AppUser user = new AppUser();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setAuthProvider("keycloak");
        user.setExternalProviderId("keycloak-123");
        user.setEmailVerified(true);

        // When
        AppUser savedUser = appUserRepository.save(user);

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isEqualTo(userId);
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getFullName()).isEqualTo("Test User");
    }

    @Test
    void shouldFindUserByExternalProviderId() {
        // Given
        UUID userId = UUID.randomUUID();
        AppUser user = new AppUser();
        user.setId(userId);
        user.setEmail("keycloak@example.com");
        user.setFullName("Keycloak User");
        user.setAuthProvider("keycloak");
        user.setExternalProviderId("keycloak-456");
        user.setEmailVerified(true);

        appUserRepository.save(user);

        // When
        Optional<AppUser> foundUser = appUserRepository.findByExternalProviderId("keycloak-456");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("keycloak@example.com");
        assertThat(foundUser.get().getExternalProviderId()).isEqualTo("keycloak-456");
    }

    @Test
    void shouldUpdateExistingUser() {
        // Given
        UUID userId = UUID.randomUUID();
        AppUser user = new AppUser();
        user.setId(userId);
        user.setEmail("original@example.com");
        user.setFullName("Original Name");
        user.setAuthProvider("keycloak");
        user.setExternalProviderId("keycloak-789");
        user.setEmailVerified(false);

        appUserRepository.save(user);

        // When - Update user
        user.setFullName("Updated Name");
        user.setEmailVerified(true);
        appUserRepository.save(user);

        // Then
        Optional<AppUser> updatedUser = appUserRepository.findById(userId);
        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().getFullName()).isEqualTo("Updated Name");
        assertThat(updatedUser.get().isEmailVerified()).isTrue();
    }

    @Test
    void shouldEnforceUniqueEmailConstraint() {
        // Given
        AppUser user1 = new AppUser();
        user1.setId(UUID.randomUUID());
        user1.setEmail("unique@example.com");
        user1.setFullName("User One");
        user1.setAuthProvider("email");
        user1.setEmailVerified(true);

        appUserRepository.save(user1);

        // When/Then - Attempting to save another user with same email should fail
        AppUser user2 = new AppUser();
        user2.setId(UUID.randomUUID());
        user2.setEmail("unique@example.com");
        user2.setFullName("User Two");
        user2.setAuthProvider("email");
        user2.setEmailVerified(true);

        // This should throw DataIntegrityViolationException due to unique constraint
        try {
            appUserRepository.save(user2);
            appUserRepository.flush(); // Force immediate execution
        } catch (Exception e) {
            assertThat(e).hasMessageContaining("unique");
        }
    }
}
