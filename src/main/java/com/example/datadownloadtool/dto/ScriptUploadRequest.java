package com.example.datadownloadtool.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ScriptUploadRequest {
    private String name;
    private String description;
    private String createdBy;
    private MultipartFile file;
}
