package com.example.datadownloadtool.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Authentication response DTO
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthResponse {
    private String token;
    private String username;
    private String role;
    private String refreshToken;
    private Long expiresIn;
}