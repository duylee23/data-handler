package com.example.datadownloadtool.controller;

import com.example.datadownloadtool.model.AuthSession;
import com.example.datadownloadtool.model.Session;
import com.example.datadownloadtool.model.User;
import com.example.datadownloadtool.api.services.AuthApiService;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    
    @Setter
    private Stage primaryStage;
    @Setter
    private HostServices hostServices;

    @Autowired
    private ApplicationContext context;
    
    @Autowired
    private AuthApiService authApiService;

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        boolean success = authApiService.login(username, password);
        if (success) {
            System.out.println("✅ Login success, token: " + AuthSession.getToken());
            // Map to User for backward compatibility
            User user = new User();
            user.setUsername(AuthSession.getUsername());
            user.setRole(AuthSession.getRole());
            Session.setCurrentUser(user);

            loadMainView();
            ((Stage) usernameField.getScene().getWindow()).close();
        } else {
            errorLabel.setText("Invalid username or password!");
        }
    }

    private void loadMainView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main_layout.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();
            
            // Setup controller
            MainLayoutController layoutController = loader.getController();
            layoutController.setHostServices(this.hostServices);
            Platform.runLater(() -> layoutController.setUser(Session.getCurrentUser()));

            // Setup scene
            Scene scene = new Scene(root, 1600, 800);
            scene.getStylesheets().addAll(
                    getClass().getResource("/css/table-style.css").toExternalForm(),
                    getClass().getResource("/css/style.css").toExternalForm()
            );
            
            //load the icon
            try {
                primaryStage.getIcons().addAll(
                    new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/logo.png")))
                );
            } catch (Exception e) {
                System.err.println("Failed to load icon: " + e.getMessage());
            }

            // Setup stage
            primaryStage.setTitle("Data Handler");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            if (e.getCause() != null) {
                e.getCause().printStackTrace();
            }
        }
    }
}