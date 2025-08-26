package com.example.datadownloadtool.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Generic API response wrapper
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse<T> {
    public boolean success;
    public String message;
    public T data;
    public int code;
    
    public ApiResponse() {}
    
    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data);
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}