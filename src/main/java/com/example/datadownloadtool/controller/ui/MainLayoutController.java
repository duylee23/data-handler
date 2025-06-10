package com.example.datadownloadtool.controller.ui;

import com.example.datadownloadtool.util.HardwareCheckerUtil;
import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class MainLayoutController {
    private boolean showingGroupList = false;
    private final FileListController fileListController;
    private final GroupListController groupListController;

    @FXML private Node fileListView;
    @FXML private Node groupListView;
    @FXML private Button btnFileList;
    @FXML private Button btnGroupList;
    @FXML private ImageView refreshIcon;
    private RotateTransition refreshAnimation;

    @FXML
    public void initialize() {
        //init the file list as default view
        fileListView.setVisible(true);
        fileListView.setManaged(true);
        groupListView.setVisible(false);
        groupListView.setManaged(false);
        btnFileList.getStyleClass().add("active");
        showSystemInfoPopup();
    }

    @FXML public void showFileList() {
        if (showingGroupList) {
            showingGroupList = false;
            switchActiveButton(btnGroupList, btnFileList);
            fileListView.setVisible(true);
            fileListView.setManaged(true);
            groupListView.setVisible(false);
            groupListView.setManaged(false);
            btnFileList.getStyleClass().add("active");
            btnGroupList.getStyleClass().remove("active");
        }
    }

    @FXML public void showGroupList() {
        if (!showingGroupList) {
            showingGroupList = true;
            switchActiveButton(btnFileList, btnGroupList);
            groupListView.setVisible(true);
            groupListView.setManaged(true);
            fileListView.setVisible(false);
            fileListView.setManaged(false);
            btnGroupList.getStyleClass().add("active");
            btnFileList.getStyleClass().remove("active");
        }
    }


    private void switchActiveButton(Button oldButton, Button newButton) {
        oldButton.getStyleClass().remove("active");
        newButton.getStyleClass().add("active");
        // Optional: add fade or glow animation
        FadeTransition ft = new FadeTransition(Duration.millis(200), newButton);
        ft.setFromValue(0.8);
        ft.setToValue(1.0);
        ft.play();
    }

    public void showSystemInfoPopup() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("System Info");
            alert.setHeaderText("Thông tin hệ thống");

            TextArea textArea = new TextArea(HardwareCheckerUtil.getSystemInfoText());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);

            alert.getDialogPane().setContent(textArea);
            alert.showAndWait();
        });
    }

    @FXML public void handleRefresh() {
        System.out.println("Refresh clicked");
        startRefreshAnimation();
        Task<Void> refreshTask = new Task<>() {
            @Override
            protected Void call() throws InterruptedException {
                if (showingGroupList) {
                    if (groupListController != null) {
                        groupListController.refreshGroupList();
                        System.out.println("Refreshing group list");
                    }
                } else {
                    if (fileListController != null) {
                        fileListController.refreshFileList();
                        System.out.println("Refreshing file list");
                    }
                }
                Thread.sleep(1000);
                return null;
            }


            @Override
            protected void succeeded() {
                stopRefreshAnimation();
            }

            @Override
            protected void failed() {
                stopRefreshAnimation();
            }
        };

        new Thread(refreshTask).start();
    }

    private void startRefreshAnimation() {
        refreshIcon.setVisible(true);
        refreshAnimation = new RotateTransition(Duration.millis(1000), refreshIcon);
        refreshAnimation.setByAngle(360);
        refreshAnimation.setCycleCount(RotateTransition.INDEFINITE);
        refreshAnimation.setInterpolator(javafx.animation.Interpolator.LINEAR);
        refreshAnimation.play();
    }

    private void stopRefreshAnimation() {
        Platform.runLater(() -> {
            if (refreshAnimation != null) {
                refreshAnimation.stop();
                refreshIcon.setRotate(0);
                refreshIcon.setVisible(false);
            }
        });
    }
}
