package com.example.datadownloadtool;

import com.example.datadownloadtool.config.StorageConfig;
import com.example.datadownloadtool.controller.LoginController;
import com.example.datadownloadtool.thread.task.ScriptTaskManager;
import com.example.datadownloadtool.util.DatabaseUtil;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
@EnableConfigurationProperties(StorageConfig.class)
@Slf4j
public class MainApp extends Application {

    private ConfigurableApplicationContext context;
    @Override
    public void init() {
        context = new SpringApplicationBuilder(MainApp.class).run();
        // ✅ Lưu lại HostServices từ JavaFX
        this.setHostServices(getHostServices());
    }
    @Setter
    private HostServices hostServices;

    @Override
    public void start(Stage primaryStage) throws Exception {
        log.info(">>> START CALLED");
        //before start, init database connection
        DatabaseUtil.initDatabase();

        // 1️⃣ Show login stage
        FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/component/login.fxml"));
        loginLoader.setControllerFactory(context::getBean);
        Parent loginRoot = loginLoader.load();

        // 3️⃣ Truyền primaryStage vào LoginController
        LoginController loginController = loginLoader.getController();
        loginController.setPrimaryStage(primaryStage);
        loginController.setHostServices(hostServices);

        Stage loginStage = new Stage();
        loginStage.setTitle("Đăng nhập");
        loginStage.setScene(new Scene(loginRoot));
        loginStage.setResizable(true);
        loginStage.showAndWait(); // ❗ Wait until login closes
    }

    @Override
    public void stop() {
        log.warn("Application STOPPING... stopping all running scripts and groups");
        ScriptTaskManager.getInstance().stopAll();
        context.close();
    }

    public static void main(String[] args) {
        try {
            Files.writeString(Path.of("log_start.txt"), "App started");
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Error while loading", e);
        }
        launch(args);
    }
}
