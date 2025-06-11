package com.example.datadownloadtool;

import com.example.datadownloadtool.config.StorageConfig;
import com.example.datadownloadtool.util.DatabaseUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.PrintStream;
import java.util.Objects;

@SpringBootApplication
@EnableConfigurationProperties(StorageConfig.class)
public class MainApp extends Application {

    private ConfigurableApplicationContext context;

    @Override
    public void init() {
        System.out.println(">>> INIT STARTING");
        context = new SpringApplicationBuilder(MainApp.class).run();
        System.out.println(">>> INIT DONE");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println(">>> START CALLED");
        //before start, init database connection
        DatabaseUtil.initDatabase();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main_layout.fxml"));
        loader.setControllerFactory(context::getBean);
        Parent root = loader.load();

        Scene scene = new Scene(root, 1600, 800);
        // Set the stylesheets for the scene
        scene.getStylesheets().addAll(
                Objects.requireNonNull(getClass().getResource("/css/table-style.css")).toExternalForm()
                , Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm()
        );
        primaryStage.setTitle("Data Handler");
        //load the icon
        try{
            primaryStage.getIcons().addAll(
                    new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/logo.png")))
            );
        } catch (Exception e) {
            System.err.println("Failed to load icon: " + e.getMessage());
        }
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        context.close();
    }

    public static void main(String[] args) {
        try {
            PrintStream fileOut = new PrintStream("debug.txt");
            System.setOut(fileOut);
            System.setErr(fileOut);
        } catch (Exception e) {
            e.printStackTrace(); // fallback
        }
        System.out.println(">>> Launching app");
        launch(args);
    }
}
