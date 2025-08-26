package com.example.datadownloadtool.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;

/**
 * Configuration class for API endpoints and settings
 */
@Configuration
@Getter
public class ApiConfig {
    
    @Value("${api.base.url:http://localhost:8081}")
    private String baseUrl;
    
    @Value("${api.timeout.connection:5}")
    private int connectionTimeoutSeconds;
    
    @Value("${api.timeout.request:10}")
    private int requestTimeoutSeconds;
    
    // API Endpoints
    public static final String AUTH_LOGIN_ENDPOINT = "/api/auth/login";
    public static final String GROUP_LIST_ENDPOINT = "/api/group/list";
    public static final String GROUP_TYPES_ENDPOINT = "/api/group/types";
    
    /**
     * Get the full URL for an endpoint
     */
    public String getFullUrl(String endpoint) {
        return baseUrl + endpoint;
    }
}