package com.example.datadownloadtool.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "scripts")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Script {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    private String description;

    private String destination;

    private String groupType;

    private String createdBy;

    private String updatedBy;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}
