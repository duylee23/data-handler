package com.example.datadownloadtool.api.exception;

/**
 * Custom exception for API-related errors
 */
public class ApiException extends RuntimeException {
    
    private final int statusCode;
    private final String responseBody;
    
    public ApiException(String message) {
        super(message);
        this.statusCode = 0;
        this.responseBody = null;
    }
    
    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
        this.responseBody = null;
    }
    
    public ApiException(String message, int statusCode, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }
    
    public ApiException(String message, int statusCode, String responseBody, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public String getResponseBody() {
        return responseBody;
    }
}