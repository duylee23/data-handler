package com.example.datadownloadtool.controller.ui;

import com.example.datadownloadtool.dao.GroupDAO;
import com.example.datadownloadtool.model.GroupRow;
import com.example.datadownloadtool.thread.executor.TaskExecutor;
import com.example.datadownloadtool.thread.task.ProgressRarTask;
import com.example.datadownloadtool.thread.task.ProgressUnzipTask;
import com.example.datadownloadtool.thread.task.RunScriptTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;
import com.example.datadownloadtool.util.CommonUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class GroupListController {
    private final CommonUtil commonUtil;
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
    @FXML public void initialize() {
        refreshGroupList();
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
    }

    @FXML
    public void handleExecute() {
        // TODO: Implement group execution
        for( GroupRow group : $groups) {
            if (group.isSelected()) {
                System.out.println("Executing group: " + group.getGroupName());
                unzipGroup(group);
            }
        }
    }

    private void unzipGroup(GroupRow group) {
        System.out.println("Unzipping group: " + group.getGroupName());
        ScheduledExecutorService unzipProgressScheduler = Executors.newSingleThreadScheduledExecutor();
        AtomicInteger unzipTicks = new AtomicInteger(0);
        final int maxUnzipTicks = 15;

        unzipProgressScheduler.scheduleAtFixedRate(() -> {
            int tick = unzipTicks.incrementAndGet();
            double progress = Math.min(0.5, tick / (double) maxUnzipTicks * 0.5);
            Platform.runLater(() -> group.getProgressBar().setProgress(progress));

            if (tick >= maxUnzipTicks) {
                unzipProgressScheduler.shutdown();
            }
        }, 0, 1, TimeUnit.SECONDS);
        File[] zipFiles = group.getGroupFolder().listFiles(f -> f.getName().endsWith(".zip") || f.getName().endsWith(".rar"));
        if (zipFiles == null || zipFiles.length == 0) return;

        ProgressBar progressBar = group.getProgressBar();
        int total = zipFiles.length;
        AtomicInteger completed = new AtomicInteger(0);

        for (File zipFile : zipFiles) {
            System.out.println( "File name zip: "+ zipFile.getName());
//            File outputDir = new File(group.getGroupFolder(), "unzipped/" + zipFile.getName().replaceAll("\\.zip|\\.rar", ""));
            File outputDir = group.getGroupFolder();
            Runnable callback = () -> {
                int done = completed.incrementAndGet();
                // When all unzips are done, trigger Python script
                if (done == total) {
                    // Dừng tiến trình giả lập progress nếu chưa hết
                    unzipProgressScheduler.shutdownNow();

                    // Bắt đầu chạy script
                    TaskExecutor.submit(new RunScriptTask(
                            group.getGroupFolder(),
                            group.getProgressBar(),
                            "D:/data_download/script/convert_pcd_3d_visual.py",
                            "D:/data_download/result",
                            success -> {
                                if (success) {
                                    Platform.runLater(() -> group.getProgressBar().setProgress(1.0));
                                    groupDAO.updateGroup(group.getGroupName(), "EXECUTED", commonUtil.getCurrentTime());
                                    commonUtil.showToast("Group : " + group.getGroupName() + " executed successfully.", true, groupListPane);
                                } else {
                                    // Không set 1.0, giữ nguyên ở 0.5
                                    groupDAO.updateGroup(group.getGroupName(), "EXECUTED", "");
                                    commonUtil.showToast("Group : " + group.getGroupName() + " execution failed.", false, groupListPane);
                                }
                            }
                    ));
                }
            };
            // Immediately show visual progress on UI to indicate it's in progress
            int started = completed.get(); // chưa cộng thêm
            double initialProgress = (double) started / total;
            Platform.runLater(() -> progressBar.setProgress(initialProgress + 0.0001)); // trick để thấy thanh chạy
            if (zipFile.getName().endsWith(".zip")) {
                TaskExecutor.submit(new ProgressUnzipTask(zipFile,outputDir, callback));
            } else if (zipFile.getName().endsWith(".rar")) {
                TaskExecutor.submit(new ProgressRarTask(zipFile, outputDir,  callback));
            }
        }
    }

    public void refreshGroupList() {
        Path dirPath = this.commonUtil.getGroupListDir();
        List<GroupRow> groupsFromDb = groupDAO.getAllGroups();
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
