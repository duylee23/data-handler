package com.example.datadownloadtool.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.storage")
@Getter
@Setter
public class StorageConfig {
    private String rootFolder;
    private String fileListFolder;
    private String groupListFolder;
}
