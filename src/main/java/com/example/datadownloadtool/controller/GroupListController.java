package com.example.datadownloadtool.controller;

import com.example.datadownloadtool.dao.GroupDAO;
import com.example.datadownloadtool.model.GroupRow;
import com.example.datadownloadtool.thread.executor.ScriptTaskFactory;
import com.example.datadownloadtool.thread.executor.TaskExecutor;
import com.example.datadownloadtool.thread.task.ProgressRarTask;
import com.example.datadownloadtool.thread.task.ProgressUnzipTask;
import com.example.datadownloadtool.util.CommonUtil;
import com.example.datadownloadtool.util.StorageUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class GroupListController {
    private final CommonUtil commonUtil;
    private final StorageUtil storageUtil = StorageUtil.getInstance();
    private final GroupDAO groupDAO = new GroupDAO();


    @FXML private VBox groupListPane;
    @FXML private TableView<GroupRow> groupTable;
    @FXML private TableColumn<GroupRow, Boolean> colSelect;
    @FXML private TableColumn<GroupRow, String> colGroupName;
    @FXML private TableColumn<GroupRow, String> colCreatedTime;
    @FXML private TableColumn<GroupRow, String> colGroupSize;
    @FXML private TableColumn<GroupRow, String> colOwner;
    @FXML private TableColumn<GroupRow, String> colGroupType;
    @FXML private TableColumn<GroupRow, String> colStatus;
    @FXML private TableColumn<GroupRow, String> colCompleteTime;
    @FXML private TableColumn<GroupRow, Integer> colGoTo;
    @FXML private TableColumn<GroupRow, ProgressBar> colProgress;
    @FXML private Button handleExecute;

    //init observable list for groups
    private final ObservableList<GroupRow> $groups = FXCollections.observableArrayList();

    public void addGroupRow(GroupRow row) {
        Platform.runLater(() -> $groups.add(row));
    }
    public ObservableList<GroupRow> getGroupList(){
        return this.$groups;
    }


    @FXML public void initialize() {
        log.warn("init group list");
        Platform.runLater(() -> {
                    log.info("groupTable.getScene().getWindow()" + groupTable.getScene().getWindow());
                    refreshGroupList();
                });
        //generate column values
        colSelect.setCellFactory(CheckBoxTableCell.forTableColumn(colSelect));
        colSelect.setCellValueFactory(cell -> cell.getValue().selectedProperty());
        colGroupName.setCellValueFactory(cell -> cell.getValue().groupNameProperty());
        colCreatedTime.setCellValueFactory(cell -> cell.getValue().createTimeProperty());
        colGroupSize.setCellValueFactory(cell -> cell.getValue().sizeProperty());
        colOwner.setCellValueFactory(cell -> cell.getValue().ownerProperty());
        colGroupType.setCellValueFactory(cell -> cell.getValue().groupTypeProperty());
        colStatus.setCellValueFactory(cell -> cell.getValue().statusProperty());
        colCompleteTime.setCellValueFactory(cell -> cell.getValue().completeTimeProperty());
        colProgress.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getProgressBar()));
        colProgress.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(ProgressBar item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(item);
                }
            }
        });

        //column alignment
        commonUtil.centerAlignColumn(colCreatedTime);
        commonUtil.centerAlignColumn(colGroupSize);
        commonUtil.centerAlignColumn(colOwner);
        commonUtil.centerAlignColumn(colGroupType);
        commonUtil.centerAlignColumn(colStatus);
        commonUtil.centerAlignColumn(colCompleteTime);

        groupTable.setItems($groups);
        groupTable.setDisable(false);
        colSelect.setEditable(true);
        groupTable.setEditable(true);

        // blur effect row group if group is pending
        groupTable.setRowFactory(table -> new TableRow<>() {
            @Override
            protected void updateItem(GroupRow item, boolean empty) {
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
    }

    @FXML
    public void handleExecute() {
        for( GroupRow group : $groups) {
            if (group.isSelected()) {
                String status = group.getStatus().get();
                if ("RUNNING".equals(status)) {
                    System.out.println("⏳ Group is already running: " + group.getGroupName());
                    continue;
                }
//                if ("EXECUTED".equals(status)) {
//                    System.out.println("✅ Group already executed: " + group.getGroupName());
//                    continue;
//                }
                updateGroupStatus("EXTRACTING", group);
                System.out.println("Executing group: " + group.getGroupName());
                extractGroup(group, () -> {
                    updateGroupStatus("RUNNING", group);
                    runScript(group);
                });
            }
        }
    }

    private void extractGroup(GroupRow group, Runnable onFinish) {
        File[] archivedFiles = group.getGroupFolder().listFiles(f -> f.getName().endsWith(".zip") || f.getName().endsWith(".rar") );
        if(archivedFiles == null || archivedFiles.length == 0) return;

        ProgressBar progressBar = group.getProgressBar();
        int totalFiles = archivedFiles.length;
        AtomicInteger completed = new AtomicInteger(0);
        for(File zipFile : archivedFiles) {
            //change later when applying configuration
            File outputDir = group.getGroupFolder();

            Consumer<Boolean> unzipCallback = success -> {
                int done = completed.incrementAndGet();
                double realProgress = done / (double) totalFiles;
                Platform.runLater(() -> progressBar.setProgress(realProgress));

                if(done == totalFiles) {
                    onFinish.run(); //when complete unzip, run script
                }
            };

            if(zipFile.getName().endsWith(".zip")) {
                TaskExecutor.submit(new ProgressUnzipTask(zipFile, outputDir, unzipCallback));
            } else if(zipFile.getName().endsWith(".rar")) {
                TaskExecutor.submit(new ProgressRarTask(zipFile, outputDir, unzipCallback));
            }
        }
    }

    private void runScript(GroupRow group) {
        ScriptTaskFactory factory = new ScriptTaskFactory();
        Runnable scriptTask = factory.createScriptTask(group, success -> {
            Platform.runLater(() -> {
                if(success) {
                    group.getProgressBar().setProgress(1.0);
                    updateGroupStatus("EXECUTED", group);
                    group.setCompleteTime(commonUtil.formatDateTime(commonUtil.getCurrentTime()));
                } else {
                    updateGroupStatus("FAILED", group);
                    group.getProgressBar().setProgress(0.5);
                }
            });
            commonUtil.showToast("Group: " + group.getGroupName() + " execution " + (success ? "successful" : "failed"), success, groupListPane);
        });
        TaskExecutor.submit(scriptTask);
    }

    private void updateGroupStatus(String status, GroupRow group) {
        group.setStatus(status);
        groupDAO.updateGroup(group.getGroupName(), status, Objects.equals(status, "EXECUTED") ? commonUtil.getCurrentTime() : "");
    }

    public void refreshGroupList() {
        Path dirPath = storageUtil.getGroupListDir();
        List<GroupRow> groupsFromDb = groupDAO.getAllGroups();
        if(dirPath == null || !Files.exists(dirPath)){
            System.out.println("Group list directory is not set or does not exist");
            return;
        }
        try (Stream<Path> paths = Files.list(dirPath)) {
            $groups.clear();
            groupTable.refresh();
            paths.forEach(path -> {
                try {
                    BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
                    String fileName = path.getFileName().toString();
                    String fileDateTime = this.commonUtil.formatFileDateTime(attr.creationTime());
                    String fileSize = Files.size(path) / 1024 / 1024 + " MB";
                    String userName = System.getProperty("user.name");
                    String groupType = "Unknown";
                    String status = "Not Started";
                    String completeTime = "N/A";
                    Optional<GroupRow> matchingGroup = groupsFromDb.stream().filter(g -> fileName.equals(g.getGroupName())).findFirst();
                    if (matchingGroup.isPresent()) {
                        GroupRow group = matchingGroup.get();
                        groupType = group.getGroupType().get();
                        status = group.getStatus().get();
                        completeTime = this.commonUtil.formatDateTime(group.getCompleteTime().get());
                    }
                    $groups.add(new GroupRow(
                            fileName,
                            fileDateTime,
                            fileSize,
                            userName,
                            groupType,
                            status,
                            completeTime,
                            path.toAbsolutePath(),
                            path.toFile()

                    ));
                    log.info("Group list Refreshed");
                } catch (IOException e) {
                    System.out.println("Failed to load file: " + path.getFileName());
                    e.printStackTrace();
                }
            });
            compareDeletedGroupInFolder(groupsFromDb, $groups);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void compareDeletedGroupInFolder(List<GroupRow> groupsFromDb, ObservableList<GroupRow> currentGroupsInUI) {
        // Lấy danh sách group name đang hiển thị trên UI
        Set<String> uiGroupNames = currentGroupsInUI.stream()
                .map(GroupRow::getGroupName)
                .collect(Collectors.toSet());

        for (GroupRow dbGroup : groupsFromDb) {
            String name = dbGroup.getGroupName();
            if (!uiGroupNames.contains(name)) {
                groupDAO.deleteGroup(name);
                groupDAO.deleteGroupFileByGroupName(name);
            }
        }
    }


}
