package com.example.datadownloadtool.api.services;

import com.example.datadownloadtool.api.client.HttpClientWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.nio.file.Path;

@Service
@Slf4j
public class DownloadScriptService {
    private final HttpClientWrapper httpClient;
    @Autowired
    public DownloadScriptService(HttpClientWrapper httpClient) {
        this.httpClient = httpClient;
    }
    /**
     * Download script ( or more than one ) from server based on group type
     * Tải ZIP scripts theo group và giải nén vào {base}/{groupName}; trả về thư mục đích
     */

}
