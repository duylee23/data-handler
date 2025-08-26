package com.example.datadownloadtool.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Group type DTO
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GroupTypeDTO {
    private Long id;
    private String name;
    private String description;
    private String category;
    
    public GroupTypeDTO() {}
    
    public GroupTypeDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}