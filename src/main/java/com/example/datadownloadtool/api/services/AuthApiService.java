package com.example.datadownloadtool.api.services;

import com.example.datadownloadtool.api.client.HttpClientWrapper;
import com.example.datadownloadtool.api.config.ApiConfig;
import com.example.datadownloadtool.api.dto.ApiResponse;
import com.example.datadownloadtool.api.dto.AuthRequest;
import com.example.datadownloadtool.api.dto.AuthResponse;
import com.example.datadownloadtool.model.AuthSession;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * Service for authentication-related API calls
 */
@Service
@Slf4j
public class AuthApiService {
    
    private final HttpClientWrapper httpClient;
    
    @Autowired
    public AuthApiService(HttpClientWrapper httpClient) {
        this.httpClient = httpClient;
    }
    
    /**
     * Authenticate user with username and password
     */
    public boolean login(String username, String password) {
        try {
            AuthRequest authRequest = new AuthRequest(username, password);
            HttpResponse<String> response = httpClient.post(ApiConfig.AUTH_LOGIN_ENDPOINT, authRequest);
            
            if (response.statusCode() == 200) {
                ApiResponse<AuthResponse> apiResponse = httpClient.parseResponse(
                    response.body(), 
                    new TypeReference<ApiResponse<AuthResponse>>() {}
                );
                
                if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                    AuthResponse authResponse = apiResponse.getData();
                    AuthSession.setToken(authResponse.getToken());
                    AuthSession.setUsername(authResponse.getUsername());
                    AuthSession.setRole(authResponse.getRole());
                    
                    log.info("Login successful for user: {}", username);
                    return true;
                }
            }
            
            log.error("Login failed for username: {}, status: {}", username, response.statusCode());
            return false;
            
        } catch (Exception e) {
            log.error("Error during login for username: {}", username, e);
            return false;
        }
    }
    
    /**
     * Authenticate user asynchronously
     */
    public CompletableFuture<Boolean> loginAsync(String username, String password) {
        AuthRequest authRequest = new AuthRequest(username, password);
        
        return httpClient.postAsync(ApiConfig.AUTH_LOGIN_ENDPOINT, authRequest)
            .thenApply(response -> {
                try {
                    if (response.statusCode() == 200) {
                        ApiResponse<AuthResponse> apiResponse = httpClient.parseResponse(
                            response.body(), 
                            new TypeReference<ApiResponse<AuthResponse>>() {}
                        );
                        
                        if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                            AuthResponse authResponse = apiResponse.getData();
                            AuthSession.setToken(authResponse.getToken());
                            AuthSession.setUsername(authResponse.getUsername());
                            AuthSession.setRole(authResponse.getRole());
                            
                            log.info("Async login successful for user: {}", username);
                            return true;
                        }
                    }
                    
                    log.error("Async login failed for username: {}, status: {}", username, response.statusCode());
                    return false;
                    
                } catch (Exception e) {
                    log.error("Error parsing login response for username: {}", username, e);
                    return false;
                }
            })
            .exceptionally(ex -> {
                log.error("Error during async login for username: {}", username, ex);
                return false;
            });
    }
    
    /**
     * Logout user by clearing session
     */
    public void logout() {
        AuthSession.clear();
        log.info("User logged out successfully");
    }
    
    /**
     * Check if user is authenticated
     */
    public boolean isAuthenticated() {
        return AuthSession.getToken() != null && !AuthSession.getToken().isEmpty();
    }
}