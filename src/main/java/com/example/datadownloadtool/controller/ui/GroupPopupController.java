package com.example.datadownloadtool.controller.ui;
import com.example.datadownloadtool.dao.GroupDAO;
import com.example.datadownloadtool.model.FileRow;
import com.example.datadownloadtool.thread.executor.TaskExecutor;
import com.example.datadownloadtool.thread.task.CreateGroupTask;
import com.example.datadownloadtool.util.CommonUtil;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


@Component
public class GroupPopupController {
    @FXML
    private TextField groupNameField;
    @FXML
    private RadioButton radio2D;
    @FXML
    private RadioButton radio3D;
    @FXML
    private VBox groupPopupPane;


    private ToggleGroup groupTypeToggle;
    private Stage popupStage;

    @Setter
    private Pane parentPane;

    @Setter
    private CommonUtil commonUtil;

    @Setter
    @Getter
    private List<FileRow> selectedFiles = new ArrayList<>();

    @Setter
    private Runnable onGroupSaved;

    public void setPopupStage(Stage popupStage) {
        this.popupStage = popupStage;
        this.popupStage.setResizable(true);
    }

    @FXML
    public void initialize() {
        groupTypeToggle = new ToggleGroup();
        radio2D.setToggleGroup(groupTypeToggle);
        radio3D.setToggleGroup(groupTypeToggle);

        // Set default selection
        radio2D.setSelected(true);
    }

    @FXML
    public void handleSave() {
        String groupName = groupNameField.getText().trim();
        String groupType = radio2D.isSelected() ? "2D" : "3D";
        if (groupName.isBlank()) {
            this.commonUtil.showToast("Please fill in all fields.", false, parentPane);
            popupStage.close();
            return;
        }

        Path groupFolder = Path.of("D:/data_download/group_list", groupName);
        try {
            if (Files.exists(groupFolder)) {
                this.commonUtil.showToast("Group folder already exists. Choose a different name.", false, parentPane);
                popupStage.close();
                return;
            }
            // create a folder for the new group
            Files.createDirectories(groupFolder);
            GroupDAO groupDAO = new GroupDAO();

            AtomicInteger completedCount = new AtomicInteger(0);
            int totalFiles = selectedFiles.size();
            for (FileRow fileRow : selectedFiles) {
                String existingGroup = groupDAO.getGroupNameByFilePath(fileRow.getPath());
                if(existingGroup != null) {
                    this.commonUtil.showToast("File " + fileRow.getPath().getFileName() + " is already in group: " + existingGroup, false, parentPane);
                    popupStage.close();
                    return; // skip this file if it already exists in a group
                }
                Path target = groupFolder.resolve(fileRow.getPath().getFileName());
                // define task
                CreateGroupTask createGroupTask = new CreateGroupTask(target, fileRow,
                        () -> {
                            groupDAO.insertGroupFile(groupName, fileRow.getPath());
                            if(completedCount.incrementAndGet() == totalFiles) {
                                //when all task done
                                groupDAO.insertGroup(groupName, groupType, "RAW");
                                commonUtil.showToast("Group created successfully!.", true, parentPane);
                                if(onGroupSaved != null) {
                                    onGroupSaved.run();
                                }
                                popupStage.close();
                            }
                        },
                        exception -> {
                            System.err.println("Failed to copy: " + exception);
                            commonUtil.showToast("Failed to copy a file: " + exception.getMessage(), false, parentPane);
                        });
                TaskExecutor.submit(createGroupTask);
                fileRow.selectedProperty().set(false);
                popupStage.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            this.commonUtil.showToast("Error saving group: " + e.getMessage(), false, groupPopupPane);
        }
    }


}
