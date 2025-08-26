package com.example.datadownloadtool.api.client;

import com.example.datadownloadtool.api.config.ApiConfig;
import com.example.datadownloadtool.api.exception.ApiException;
import com.example.datadownloadtool.model.AuthSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Wrapper class for HTTP client operations with common configurations
 */
@Component
@Slf4j
public class HttpClientWrapper {
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ApiConfig apiConfig;
    
    @Autowired
    public HttpClientWrapper(ApiConfig apiConfig) {
        this.apiConfig = apiConfig;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(apiConfig.getConnectionTimeoutSeconds()))
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Execute a GET request
     */
    public HttpResponse<String> get(String endpoint) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiConfig.getFullUrl(endpoint)))
                .timeout(Duration.ofSeconds(apiConfig.getRequestTimeoutSeconds()))
                .header("Content-Type", "application/json")
                .header("Authorization", getAuthorizationHeader())
                .GET()
                .build();
        
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
    
    /**
     * Execute a GET request asynchronously
     */
    public CompletableFuture<HttpResponse<String>> getAsync(String endpoint) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiConfig.getFullUrl(endpoint)))
                .timeout(Duration.ofSeconds(apiConfig.getRequestTimeoutSeconds()))
                .header("Content-Type", "application/json")
                .header("Authorization", getAuthorizationHeader())
                .GET()
                .build();
        
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }
    
    /**
     * Execute a POST request
     */
    public HttpResponse<String> post(String endpoint, Object requestBody) throws Exception {
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiConfig.getFullUrl(endpoint)))
                .timeout(Duration.ofSeconds(apiConfig.getRequestTimeoutSeconds()))
                .header("Content-Type", "application/json")
                .header("Authorization", getAuthorizationHeader())
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
    
    /**
     * Execute a POST request with string body
     */
    public HttpResponse<String> post(String endpoint, String requestBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiConfig.getFullUrl(endpoint)))
                .timeout(Duration.ofSeconds(apiConfig.getRequestTimeoutSeconds()))
                .header("Content-Type", "application/json")
                .header("Authorization", getAuthorizationHeader())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
    
    /**
     * Execute a POST request asynchronously
     */
    public CompletableFuture<HttpResponse<String>> postAsync(String endpoint, Object requestBody) {
        try {
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiConfig.getFullUrl(endpoint)))
                    .timeout(Duration.ofSeconds(apiConfig.getRequestTimeoutSeconds()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", getAuthorizationHeader())
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
            
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            log.error("Error creating async POST request", e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * Parse JSON response to object
     */
    public <T> T parseResponse(String jsonResponse, Class<T> responseType) throws Exception {
        return objectMapper.readValue(jsonResponse, responseType);
    }
    
    /**
     * Parse JSON response to object with TypeReference
     */
    public <T> T parseResponse(String jsonResponse, com.fasterxml.jackson.core.type.TypeReference<T> typeReference) throws Exception {
        return objectMapper.readValue(jsonResponse, typeReference);
    }
    
    /**
     * Get authorization header value
     */
    private String getAuthorizationHeader() {
        String token = AuthSession.getToken();
        return token != null ? "Bearer " + token : "";
    }
    
    /**
     * Get the ObjectMapper instance
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
    
    /**
     * Check if response is successful, throw ApiException if not
     */
    public void validateResponse(HttpResponse<String> response) throws ApiException {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String errorMessage = String.format("API call failed with status %d", response.statusCode());
            throw new ApiException(errorMessage, response.statusCode(), response.body());
        }
    }
    
    /**
     * Execute GET request and validate response
     */
    public HttpResponse<String> getValidated(String endpoint) throws Exception {
        HttpResponse<String> response = get(endpoint);
        validateResponse(response);
        return response;
    }
    
    /**
     * Execute POST request and validate response
     */
    public HttpResponse<String> postValidated(String endpoint, Object requestBody) throws Exception {
        HttpResponse<String> response = post(endpoint, requestBody);
        validateResponse(response);
        return response;
    }
}