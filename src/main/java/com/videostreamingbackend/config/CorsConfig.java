package com.videostreamingbackend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class CorsConfig {
    private final AppProperties appProperties;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        //Allow only the origins configured per environment
        config.setAllowedOrigins(appProperties.getCors().getAllowedOrigins());

        //Standard HTTP methods
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS","PATCH"));

        //Allow all headers the brower might send
        config.setAllowedHeaders(List.of("*"));

        //Expose Auth header so front end can read it
        config.setExposedHeaders(List.of("Authorization", "Cache-Control", "Content-Type", "Content-Range", "Accept-Range"));

        //Cashe preflight response for 1 hours
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);

        return source;
    }
}
