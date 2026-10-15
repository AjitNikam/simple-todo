package com.example.simpletodo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {
    @Bean
    WebMvcConfigurer corsConfigurer(
            @Value("${app.cors.allowed-origin}") String allowedOrigin) {
        if (allowedOrigin.isBlank() || allowedOrigin.contains("*") || allowedOrigin.contains(",")) {
            throw new IllegalStateException("CORS_ALLOWED_ORIGINS must be exactly one explicit origin.");
        }
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(allowedOrigin)
                        .allowedMethods("GET", "POST", "OPTIONS", "PUT", "PATCH")
                        .allowedHeaders("Accept", "Content-Type")
                        .allowCredentials(false)
                        .maxAge(3600);
            }
        };
    }
}
