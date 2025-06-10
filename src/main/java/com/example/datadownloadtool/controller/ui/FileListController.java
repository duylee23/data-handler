package com.example.datadownloadtool.controller.ui;

import com.example.datadownloadtool.dao.GroupDAO;
import com.example.datadownloadtool.thread.executor.TaskExecutor;
import com.example.datadownloadtool.thread.task.ImportTask;
import com.example.datadownloadtool.util.CommonUtil;
import com.example.datadownloadtool.model.FileRow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class FileListController {
    //init observable list for files
    private final ObservableList<FileRow> $files = FXCollections.observableArrayList();
    private final CommonUtil commonUtil;
    private final GroupListController groupListController;
    private final GroupDAO groupDAO = new GroupDAO();

    @FXML
    private TableView<FileRow> fileTable;
    @FXML private TableColumn<FileRow, Boolean> colSelect;
    @FXML private TableColumn<FileRow, String> colName;
    @FXML private TableColumn<FileRow, String> colCreateTime;
    @FXML private TableColumn<FileRow, String> colSize;
    @FXML private TableColumn<FileRow, String> colOwner;
    @FXML private TableColumn<FileRow, String> colGroup;
    @FXML private VBox fileListPane;

    @FXML public void initialize() {
        //create directories for files if not exist
        this.commonUtil.initStoragePath();

        // setup columns
        colSelect.setCellFactory(CheckBoxTableCell.forTableColumn(colSelect));
        colSelect.setCellValueFactory(cell -> cell.getValue().selectedProperty());
        colName.setCellValueFactory(cell -> cell.getValue().nameProperty());
        colCreateTime.setCellValueFactory(cell -> cell.getValue().createTimeProperty());
        colSize.setCellValueFactory(cell -> cell.getValue().sizeProperty());
        colOwner.setCellValueFactory(cell -> cell.getValue().ownerProperty());
        colGroup.setCellValueFactory(cell -> cell.getValue().groupNameProperty());

        // column alignment
        commonUtil.centerAlignColumn(colCreateTime);
        commonUtil.centerAlignColumn(colSize);
        commonUtil.centerAlignColumn(colOwner);
        commonUtil.centerAlignColumn(colGroup);
        fileTable.setItems($files);
        fileTable.setDisable(false);
        colSelect.setEditable(true);
        fileTable.setEditable(true);

        // blur effect row file if file is pending
        fileTable.setRowFactory(table -> new TableRow<>() {
            @Override
            protected void updateItem(FileRow item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setOpacity(1.0);
                    return;
                }
                // delete previous binding
                opacityProperty().unbind();
                // Re-binding based on pending status
                item.isPendingProperty().addListener((obs, wasPending, isNowPending) -> {
                    setOpacity(isNowPending ? 0.4 : 1.0);
                });
                setOpacity(item.isPending() ? 0.4 : 1.0);
            }
        });

        // Auto load from directory
        refreshFileList();
    }

    @FXML public void handleImport() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("ZIP Files", "*.zip")
        );
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Downloads"));
        //allow selecting multiple files
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(fileTable.getScene().getWindow());
        if (selectedFiles != null) {
            Path dirPath = this.commonUtil.getFileListDir();
            for (File file : selectedFiles) {
                // if file already exists, skip
                if($files.stream().anyMatch(f -> f.getName().equals(file.getName()))) { continue;}

                // add new file to observable list
                FileRow row = new FileRow(
                        file.getName(),
                        this.commonUtil.formatFileDateTime(Files.readAttributes(Path.of(file.getPath()), BasicFileAttributes.class).creationTime()),
                        (file.length() / 1024 / 1024) + "MB",
                        System.getProperty("user.name"),
                        "",
                        file.toPath()
                );
                row.setPending(true);
                $files.add(row);
                // create target path
                Path targetPath = dirPath.resolve(file.getName());
                ImportTask importTask = new ImportTask(
                        file.toPath(),
                        targetPath,
                        row,
                        () -> commonUtil.showToast("Imported success: " + file.getName(), true, fileListPane),
                        (ex) -> commonUtil.showToast("Failed to import: " + ex.getMessage(), false, fileListPane));
                TaskExecutor.submit(importTask);
            }
        }
        System.out.println("Import clicked");
    }

    @FXML public void handleCreateGroup() {
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/component/group_popup.fxml"));
            Parent root = fxmlLoader.load();
            GroupPopupController groupPopupController = fxmlLoader.getController();
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Create Group");
            popupStage.setScene(new Scene(root));
            groupPopupController.setPopupStage(popupStage);
            List<FileRow> selectedFiles = this.getSelectedFiles();
            if (selectedFiles.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "No files selected for grouping.", ButtonType.OK);
                alert.showAndWait();
                return;
            }
            groupPopupController.setSelectedFiles(selectedFiles);

            // get mainStage from paren view
            groupPopupController.setParentPane(fileListPane);

            // inject CommonUtil
            groupPopupController.setCommonUtil(this.commonUtil);

            // inject callback function
            groupPopupController.setOnGroupSaved(() -> {
                groupListController.refreshGroupList();
                this.refreshGroupNames();
            });

            popupStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @FXML public void handleSendResult() {
        System.out.println("Send result clicked");
    }

    @FXML public void handleDelete() {
        List<FileRow> selected = $files.stream().filter(FileRow::isSelected).toList();
        if(selected.isEmpty()){
            Alert alert = new Alert(Alert.AlertType.WARNING, "No files selected for deletion.", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        showDeleteConfirmation(selected);
    }



    //load file từ thư mục trong máy
    public void refreshFileList() {
        Path dirPath = this.commonUtil.getFileListDir();
        Map<Path, String> fileGroupMap = this.groupDAO.getGroupFileMap();
        try (Stream<Path> paths = Files.list(dirPath)) {
            $files.clear();
            paths.forEach(path -> {
                try {
                    BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
                    String fileName = path.getFileName().toString();
                    String fileDateTime = this.commonUtil.formatFileDateTime(attr.creationTime());
                    String fileSize = Files.size(path) / 1024 / 1024 + " MB";
                    String userName = System.getProperty("user.name");
                    // Get group name from map if exists
                    String groupName = fileGroupMap.getOrDefault(path,"");

                    $files.add(new FileRow(
                            fileName,
                            fileDateTime,
                            fileSize,
                            userName,
                            groupName, // giả định
                            path.toAbsolutePath()
                    ));
                } catch (IOException e) {
                    System.out.println("Failed to load file: " + path.getFileName());
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private List<FileRow> getSelectedFiles() {
        return $files.stream()
                .filter(FileRow::isSelected)
                .toList();
    }

    private void showDeleteConfirmation(List<FileRow> selectedFiles) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Confirm Deletion");
        //UI dialog Vbox
        VBox container  = new VBox(25);
        container.setPadding(new Insets(20));
        Label confirmLabel = new Label("\uD83D\uDCC1  Are you sure you want to delete these files?");
        confirmLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // === Scrollable file list ===
        VBox fileListBox = new VBox(5);
        for (FileRow file : selectedFiles) {
            Label fileLabel = new Label("• " + file.getName());
            fileLabel.setStyle("-fx-font-size: 13px;");
            fileListBox.getChildren().add(fileLabel);
        }

        ScrollPane scrollPane = new ScrollPane(fileListBox);
        scrollPane.setPrefHeight(200);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #f9f9f9; -fx-border-color: lightgray; -fx-border-radius: 5px;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // === Layout ===
        container.getChildren().addAll(confirmLabel, scrollPane);
        dialog.getDialogPane().setContent(container);

        ButtonType confirmButton = new ButtonType("✅ Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("❌ Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButton, cancelButton);

        //handle result
        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get() == confirmButton) {
            for(FileRow file : selectedFiles) {
                try {
                    if (Files.isDirectory(file.getPath())) {
                        deleteDirectoryRecursively(file.getPath());
                    } else {
                        Files.deleteIfExists(file.getPath());
                    }
                    groupDAO.deleteFileByPath(file.getPath());
                    this.commonUtil.showToast("Delete file successfully " , true, fileListPane );
                } catch (IOException e) {
                    e.printStackTrace();
                    this.commonUtil.showToast("Delete file failed! " , false, fileListPane);
                }
            }
            refreshFileList();
        }
    }

    public void refreshGroupNames(){
        Map<Path, String> groupFileMap = this.groupDAO.getGroupFileMap();
        for (FileRow file : $files) {
            String newGroupName = groupFileMap.getOrDefault(file.getPath(), "");
            file.groupNameProperty().set(newGroupName);
        }
        System.out.println("✅ File group names updated in-place");
    }

    private void deleteDirectoryRecursively(Path dir) throws IOException {
        if (Files.exists(dir)) {
            // Duyệt từ file con → xóa từ dưới lên
            try (Stream<Path> walk = Files.walk(dir)) {
                walk.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException e) {
                                throw new UncheckedIOException("Failed to delete: " + path, e);
                            }
                        });
            }
        }
    }


}
