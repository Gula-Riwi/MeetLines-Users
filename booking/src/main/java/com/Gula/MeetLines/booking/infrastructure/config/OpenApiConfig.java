package com.Gula.MeetLines.booking.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for the Booking module.
 * 
 * <p>Configures interactive API documentation with JWT Bearer authentication support.
 * Access Swagger UI at: http://localhost:{server.port}/swagger-ui.html</p>
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8082}")
    private String serverPort;

    @Bean
    public OpenAPI bookingOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .info(new Info()
                        .title("MeetLines Booking API")
                        .version("1.0.0")
                        .description("""
                                Appointment Booking and Management API for MeetLines.
                                
                                ## Features
                                - Book appointments with specific employees
                                - Check available time slots
                                - View and manage appointments
                                - Get project working hours
                                
                                ## Authentication
                                This API uses JWT Bearer tokens from Keycloak. To authenticate:
                                1. Obtain a token from Keycloak
                                2. Click the "Authorize" button
                                3. Enter your token in the format: `your-jwt-token`
                                """)
                        .contact(new Contact()
                                .name("MeetLines Team")
                                .email("support@meetlines.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token obtained from Keycloak")));
    }
}
