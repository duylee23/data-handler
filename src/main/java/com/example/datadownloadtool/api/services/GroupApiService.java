package com.example.datadownloadtool.api.services;

import com.example.datadownloadtool.api.client.HttpClientWrapper;
import com.example.datadownloadtool.api.config.ApiConfig;
import com.example.datadownloadtool.api.dto.GroupTypeDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service for group-related API calls
 */
@Service
@Slf4j
public class GroupApiService {
    
    private final HttpClientWrapper httpClient;
    
    @Autowired
    public GroupApiService(HttpClientWrapper httpClient) {
        this.httpClient = httpClient;
    }
    
    /**
     * Get list of available group types
     */
    public List<GroupTypeDTO> getGroupTypes() {
        try {
            HttpResponse<String> response = httpClient.get(ApiConfig.GROUP_LIST_ENDPOINT);
            
            if (response.statusCode() == 200) {
                return parseGroupTypesResponse(response.body());
            }
            
            log.error("Failed to fetch group types, status: {}", response.statusCode());
            return getDefaultGroupTypes();
            
        } catch (Exception e) {
            log.error("Error fetching group types", e);
            return getDefaultGroupTypes();
        }
    }
    
    /**
     * Get list of available group types asynchronously
     */
    public CompletableFuture<List<GroupTypeDTO>> getGroupTypesAsync() {
        return httpClient.getAsync(ApiConfig.GROUP_LIST_ENDPOINT)
            .thenApply(response -> {
                try {
                    if (response.statusCode() == 200) {
                        return parseGroupTypesResponse(response.body());
                    }
                    
                    log.error("Failed to fetch group types asynchronously, status: {}", response.statusCode());
                    return getDefaultGroupTypes();
                    
                } catch (Exception e) {
                    log.error("Error parsing group types response", e);
                    return getDefaultGroupTypes();
                }
            })
            .exceptionally(ex -> {
                log.error("Error during async group types fetch", ex);
                return getDefaultGroupTypes();
            });
    }
    
    /**
     * Parse group types response, handling both DTO list and string list formats
     */
    private List<GroupTypeDTO> parseGroupTypesResponse(String responseBody) throws Exception {
        try {
            // Try parsing as List<GroupTypeDTO> first
            List<GroupTypeDTO> groupTypes = httpClient.parseResponse(
                responseBody,
                    new TypeReference<>() {
                    }
            );
            log.debug("Successfully parsed {} group types from DTO format", groupTypes.size());
            return groupTypes;
            
        } catch (Exception e) {
            // Fallback to parsing as List<String>
            try {
                List<String> groupTypeNames = httpClient.parseResponse(
                    responseBody, 
                    new TypeReference<List<String>>() {}
                );
                
                List<GroupTypeDTO> groupTypes = new ArrayList<>();
                for (String name : groupTypeNames) {
                    groupTypes.add(new GroupTypeDTO(null, name));
                }
                
                log.debug("Successfully parsed {} group types from string format", groupTypes.size());
                return groupTypes;
                
            } catch (Exception fallbackException) {
                log.error("Failed to parse group types in both formats", fallbackException);
                throw fallbackException;
            }
        }
    }
    
    /**
     * Get default group types as fallback
     */
    private List<GroupTypeDTO> getDefaultGroupTypes() {
        List<GroupTypeDTO> defaultTypes = new ArrayList<>();
        defaultTypes.add(new GroupTypeDTO(null, "2D"));
        defaultTypes.add(new GroupTypeDTO(null, "3D"));
        
        log.info("Using default group types as fallback");
        return defaultTypes;
    }
    
    /**
     * Convert GroupTypeDTO list to name pair format for UI compatibility
     */
    public static class NamePair {
        private final Long id;
        private final String displayName;
        
        public NamePair(Long id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }
        
        public Long getId() { return id; }
        public String getDisplayName() { return displayName; }
    }
    
    /**
     * Convert GroupTypeDTO list to NamePair list for backward compatibility
     */
    public List<NamePair> convertToNamePairs(List<GroupTypeDTO> groupTypes) {
        List<NamePair> namePairs = new ArrayList<>();
        for (GroupTypeDTO dto : groupTypes) {
            namePairs.add(new NamePair(dto.getId(), dto.getName()));
        }
        return namePairs;
    }
}