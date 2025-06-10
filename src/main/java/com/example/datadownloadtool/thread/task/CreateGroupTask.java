package com.example.datadownloadtool.thread.task;

import com.example.datadownloadtool.dao.GroupDAO;
import com.example.datadownloadtool.model.FileRow;
import javafx.application.Platform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

public class CreateGroupTask implements Runnable {
    private final Path targetPath;
    private final FileRow fileRow;
    private final Runnable onSuccess;
    private final Consumer<Exception> onError;

    public CreateGroupTask(Path targetPath, FileRow fileRow, Runnable onSuccess, Consumer<Exception> onError) {
        this.targetPath = targetPath;
        this.fileRow = fileRow;
        this.onSuccess = onSuccess;
        this.onError = onError;
    }

    @Override
    public void run() {
        try{
            Files.copy(fileRow.getPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            fileRow.selectedProperty().set(false);
            fileRow.setPending(false);
            if (onSuccess != null) onSuccess.run();
        } catch (IOException e) {
            Platform.runLater(() -> {
                if (onError != null) onError.accept(e);
            });
        }
    }
}
