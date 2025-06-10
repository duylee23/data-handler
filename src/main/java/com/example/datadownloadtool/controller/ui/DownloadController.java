package com.example.datadownloadtool.controller.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.net.URLEncoder;

@Component
public class DownloadController {

    @FXML
    private TextField keyField;

    @FXML
    private Label resultLabel;

    private final RestTemplate restTemplate = new RestTemplate();

    @FXML
    public void handleDownload() {
        String key = keyField.getText().trim();

        if (key.isEmpty()) {
            resultLabel.setText("Key is required.");
            return;
        }
        //open directory chooser
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select a target folder");

        // get current stage
        Stage stage = (Stage) keyField.getScene().getWindow();
        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory == null) {
            resultLabel.setText("No directory selected.");
            return;
        }
        String targetPath = selectedDirectory.getAbsolutePath();



        // Gửi POST request đến Spring Boot backend
        new Thread(() -> {
            try {
                String url = "http://localhost:8080/api/s3/download?key=" + URLEncoder.encode(key, "UTF-8")
                        + "&targetPath=" + URLEncoder.encode(targetPath, "UTF-8");

                ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);

                Platform.runLater(() -> resultLabel.setText(response.getBody()));
            } catch (Exception e) {
                Platform.runLater(() -> resultLabel.setText("Error: " + e.getMessage()));
            }
        }).start();
    }

    @FXML private TextField targetPathField;

    @FXML
    public void handleBrowse() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Target Folder");
        Stage stage = (Stage) keyField.getScene().getWindow();
        File selectedDir = directoryChooser.showDialog(stage);
        if (selectedDir != null) {
            targetPathField.setText(selectedDir.getAbsolutePath());
        }
    }
}
