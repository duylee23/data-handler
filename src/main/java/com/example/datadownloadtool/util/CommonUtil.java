package com.example.datadownloadtool.util;

import com.example.datadownloadtool.config.StorageConfig;
import com.example.datadownloadtool.model.FileRow;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
@Getter
@Setter
public class CommonUtil {
    private final StorageConfig storageConfig;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy h:mm a");
    private static final List<Popup> activeToasts = new ArrayList<>();

    private Path fileListDir;
    private Path groupListDir;

    // Canh giữa text trong cột
    public <S, T> void centerAlignColumn(TableColumn<S, T> column) {
        column.setCellFactory(col -> {
            TableCell<S, T> cell = new TableCell<>() {
                @Override
                protected void updateItem(T item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.toString());
                    setAlignment(Pos.CENTER);
                }
            };
            return cell;
        });
    }

    public String formatFileDateTime(FileTime fileTime) {
        if (fileTime == null) {
            return "";
        }
        return DATE_FORMATTER.format(LocalDateTime.ofInstant(fileTime.toInstant(), ZoneId.systemDefault()));
    }
    public String formatDateTime(String dateTime) {
        if (dateTime == null || dateTime.isBlank()) {
            return "";
        }
        return DATE_FORMATTER.format(LocalDateTime.parse(dateTime));
    }

    public void initStoragePath(){
        try{
            Path baseDir = Paths.get("D:/",storageConfig.getRootFolder());
            fileListDir = baseDir.resolve(storageConfig.getFileListFolder());
            groupListDir = baseDir.resolve(storageConfig.getGroupListFolder());
            Files.createDirectories(fileListDir);
            Files.createDirectories(groupListDir);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize file list directories", e);
        }
    }

    public void showToast(String message, boolean success, Node relativeToNode) {
        // Icon: ✔ hoặc ✖
        Label iconLabel = new Label(success ? "\u2714" : "\u2716"); // ✔ : ✖
        iconLabel.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-padding: 0 10 0 0;" // padding right 10
        );

        Label toastLabel = new Label(message);
        toastLabel.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;"
        );

        HBox messageBox = new HBox(iconLabel, toastLabel);
        messageBox.setAlignment(Pos.CENTER_LEFT);
        messageBox.setSpacing(5);
        messageBox.setPadding(new Insets(10, 20, 5, 20)); // top-right-bottom-left

        Region timerBar = new Region();
        timerBar.setPrefHeight(3);
        timerBar.setStyle("-fx-background-color: white;");
        timerBar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(timerBar, Priority.ALWAYS);

        VBox toastBox = new VBox(messageBox, timerBar);
        toastBox.setStyle(
                "-fx-background-color: " + (success ? "#4BB543" : "#FF5C5C") + ";" +
                        "-fx-background-radius: 10px;" +
                        "-fx-padding: 0;" +
                        "-fx-spacing: 5;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 8, 0, 0, 2);"
        );
        toastBox.setOpacity(0);
        toastBox.setTranslateX(30);

        Popup popup = new Popup();
        popup.getContent().add(toastBox);
        popup.setAutoFix(true);
        popup.setAutoHide(false);

        final double durationSeconds = 3;
        // Fade-out khi xong
        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), toastBox);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(ev -> {
            popup.hide();
            activeToasts.remove(popup);
            repositionToasts(relativeToNode); //cập nhật lại vị trí các toast còn lại
        });

        // Thanh thời gian
        ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(durationSeconds), timerBar);
        scaleTransition.setFromX(1.0);
        scaleTransition.setToX(0.0);
        scaleTransition.setInterpolator(Interpolator.LINEAR);
        scaleTransition.setOnFinished(ev -> fadeOut.play());

        // Hover để giữ lại
        toastBox.setOnMouseEntered(e -> scaleTransition.pause());
        toastBox.setOnMouseExited(e -> scaleTransition.play());

        // Hiệu ứng xuất hiện
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toastBox);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), toastBox);
        slideIn.setFromX(30);
        slideIn.setToX(0);

        ParallelTransition appear = new ParallelTransition(fadeIn, slideIn);

        Platform.runLater(() -> {
            toastBox.applyCss();
            toastBox.layout();

            Bounds nodeBounds = relativeToNode.localToScreen(relativeToNode.getBoundsInLocal());
            double x = nodeBounds.getMaxX() - 180;
            double y = nodeBounds.getMinY() + 20 + activeToasts.size() * 60;

            popup.show(relativeToNode.getScene().getWindow(), x, y);
            activeToasts.add(popup); //lưu lại popup đang hiển thị
            appear.play();
            scaleTransition.play();
        });
    }

    public static <T> Callback<TableColumn<T, String>, TableCell<T, String>> createStatusCellFactory() {
        return column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                System.out.println("STATUS RENDERED: " + status);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

                    String normalized = status.trim().toUpperCase();

                    switch (normalized) {
                        case "RAW" -> setStyle("-fx-text-fill: dodgerblue;");
                        case "PENDING", "NOT STARTED" -> setStyle("-fx-text-fill: mediumpurple;");
                        case "EXECUTED" -> setStyle("-fx-text-fill: green;");
                        default -> setStyle("-fx-text-fill: gray;");
                    }
                }
            }
        };
    }

    public String getCurrentTime () {
        return LocalDateTime.now().toString();
    }

    private void repositionToasts(Node relativeToNode) {
        for (int i = 0; i < activeToasts.size(); i++) {
            Popup popup = activeToasts.get(i);
            Bounds nodeBounds = relativeToNode.localToScreen(relativeToNode.getBoundsInLocal());
            double x = nodeBounds.getMaxX() - 200;
            double y = nodeBounds.getMinY() + 20 + i * 60;

            popup.setX(x);
            popup.setY(y);
        }
    }

}
