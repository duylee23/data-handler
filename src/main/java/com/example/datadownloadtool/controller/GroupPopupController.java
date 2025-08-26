package com.example.datadownloadtool.controller;

import com.example.datadownloadtool.api.dto.ApiResponse;
import com.example.datadownloadtool.dao.GroupDAO;
import com.example.datadownloadtool.model.AuthSession;
import com.example.datadownloadtool.model.FileRow;
import com.example.datadownloadtool.model.GroupRow;
import com.example.datadownloadtool.thread.executor.TaskExecutor;
import com.example.datadownloadtool.thread.task.CreateGroupTask;
import com.example.datadownloadtool.util.CommonUtil;
import com.example.datadownloadtool.util.StorageUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;


@Component
@RequiredArgsConstructor
public class GroupPopupController {
    @FXML
    private TextField groupNameField;
    @FXML
    private VBox groupPopupPane;
    @FXML
    private FlowPane groupTypeBox;

    private ToggleGroup groupTypeToggle;
    private Stage popupStage;
    private final StorageUtil storageUtil = StorageUtil.getInstance();
    @Setter
    private Pane parentPane;

    @Setter
    private CommonUtil commonUtil;

    @Setter
    @Getter
    private List<FileRow> selectedFiles = new ArrayList<>();

    @Setter
    private Runnable onGroupSaved;

    @Setter
    private Consumer<GroupRow> onGroupRowCreated;


    // === cấu hình API ===
    // TODO: đổi URL này theo API của bạn, ví dụ: http://localhost:8080/api/group/types
    private static final String GROUP_TYPES_API = "http://localhost:8081/api/group/types";
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper mapper = new ObjectMapper();

    public void setPopupStage(Stage popupStage) {
        this.popupStage = popupStage;
        this.popupStage.setResizable(true);
    }

    @FXML
    public void initialize() {
        groupTypeToggle = new ToggleGroup();
        loadGroupTypesAsync();
    }

    @FXML
    public void handleSave() {
        String groupName = groupNameField.getText().trim();
        Toggle selected = groupTypeToggle.getSelectedToggle();

        if (groupName.isBlank() || selected == null) {
            this.commonUtil.showToast("Please fill in all fields.", false, parentPane);
            popupStage.close();
            return;
        }

        NamePair chosen = (NamePair) selected.getUserData();
        // Nếu API bạn cần gửi id: dùng chosen.id()
        // Nếu API bạn cần gửi name: dùng chosen.displayName()
        String groupType = chosen.displayName();

        Path groupFolder = Path.of(storageUtil.getGroupListDir().toString(), groupName);
        try {
            if (Files.exists(groupFolder)) {
                this.commonUtil.showToast("Group folder already exists. Choose a different name.", false, parentPane);
                popupStage.close();
                return;
            }
            Files.createDirectories(groupFolder);
            GroupDAO groupDAO = new GroupDAO();

            GroupRow pendingGroup = new GroupRow(
                    groupName,
                    "-", "-", "-", // createTime, size, owner
                    groupType,
                    "Pending",
                    "-", // completeTime
                    null, // path
                    groupFolder.toFile()
            );
            pendingGroup.setIsPending(true);

            if (onGroupRowCreated != null) {
                Platform.runLater(() -> onGroupRowCreated.accept(pendingGroup));
            }

            AtomicInteger completedCount = new AtomicInteger(0);
            int totalFiles = selectedFiles.size();
            for (FileRow fileRow : selectedFiles) {
                String existingGroup = groupDAO.getGroupNameByFilePath(fileRow.getPath());
                if (existingGroup != null) {
                    this.commonUtil.showToast(
                            "File " + fileRow.getPath().getFileName() + " is already in group: " + existingGroup,
                            false, parentPane);
                    popupStage.close();
                    return;
                }

                Path target = groupFolder.resolve(fileRow.getPath().getFileName());
                CreateGroupTask task = new CreateGroupTask(
                        target, fileRow,
                        () -> {
                            groupDAO.insertGroupFile(groupName, fileRow.getPath());
                            if (completedCount.incrementAndGet() == totalFiles) {
                                groupDAO.insertGroup(groupName, groupType, "RAW");
                                commonUtil.showToast("Group created successfully!.", true, parentPane);
                                if (onGroupSaved != null) onGroupSaved.run();
                                popupStage.close();
                            }
                        },
                        ex -> {
                            System.err.println("Failed to copy: " + ex);
                            commonUtil.showToast("Failed to copy a file: " + ex.getMessage(), false, parentPane);
                        }
                );
                TaskExecutor.submit(task);
                fileRow.selectedProperty().set(false);
            }
        } catch (IOException e) {
            e.printStackTrace();
            this.commonUtil.showToast("Error saving group: " + e.getMessage(), false, groupPopupPane);
        }
    }

    private void loadGroupTypesAsync() {
        String token = AuthSession.getToken();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(GROUP_TYPES_API))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", "Bearer " + token)   // ✅ thêm dòng này
                .header("Accept", "application/json")
                .GET()
                .build();

        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
                        if (res.statusCode() != 200) {
                            throw new RuntimeException("Fetch group types failed: " + res.statusCode());
                        }

                        // Tùy response:
                        // 1) [{ "id": 1, "name": "2D" }, ...]
                        // 1. Thử parse dạng List<GroupTypeDTO> bên trong data
                        String body = res.body();
                        try {
                            ApiResponse<List<GroupTypeDTO>> wrapper =
                                    mapper.readValue(body, new TypeReference<ApiResponse<List<GroupTypeDTO>>>() {});
                            return toNamePairsFromDto(wrapper.data);
                        } catch (Exception e1) {
                            // 2. Nếu không parse được thì thử parse List<String>
                            ApiResponse<List<String>> wrapper =
                                    mapper.readValue(body, new TypeReference<ApiResponse<List<String>>>() {});
                            return toNamePairsFromStrings(wrapper.data);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .thenAccept(namePairs -> Platform.runLater(() -> renderTypeRadioButtons(namePairs)))
                .exceptionally(ex -> {
                    Platform.runLater(() ->
                            commonUtil.showToast("Load group types failed: " + ex.getMessage(), false, parentPane));
                    // fallback tối thiểu để không chặn luồng tạo group
                    Platform.runLater(() -> renderTypeRadioButtons(List.of(new NamePair(null, "2D"), new NamePair(null, "3D"))));
                    return null;
                });
    }

    private void renderTypeRadioButtons(List<NamePair> types) {
        groupTypeBox.getChildren().clear();
        groupTypeToggle.getToggles().clear();

        for (NamePair np : types) {
            RadioButton rb = new RadioButton(np.displayName());
            rb.setUserData(np); // lưu cả id & name
            rb.setToggleGroup(groupTypeToggle);
            groupTypeBox.getChildren().add(rb);
        }
        // Chọn mặc định phần tử đầu tiên (nếu có)
        if (!groupTypeToggle.getToggles().isEmpty()) {
            groupTypeToggle.selectToggle(groupTypeToggle.getToggles().get(0));
        }
    }

    // ===== helper models =====
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GroupTypeDTO(Long id, String name) {}

    public record NamePair(Long id, String displayName) {}

    private static List<NamePair> toNamePairsFromDto(List<GroupTypeDTO> list) {
        List<NamePair> out = new ArrayList<>();
        for (GroupTypeDTO t : list) {
            out.add(new NamePair(t.id(), t.name()));
        }
        return out;
    }

    private static List<NamePair> toNamePairsFromStrings(List<String> names) {
        List<NamePair> out = new ArrayList<>();
        for (String n : names) out.add(new NamePair(null, n));
        return out;
    }


}
