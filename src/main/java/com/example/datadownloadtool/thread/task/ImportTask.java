package com.example.datadownloadtool.thread.task;

import com.example.datadownloadtool.model.FileRow;
import com.example.datadownloadtool.util.CommonUtil;
import javafx.application.Platform;
import javafx.scene.Node;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

public class ImportTask implements Runnable {
    private final Path sourceFile;
    private final Path targetPath;
    private final FileRow fileRow;
    private final Runnable onSuccessCallback;
    private final Consumer<Exception> onErrorCallback;

    public ImportTask(Path sourceFile, Path targetPath, FileRow fileRow,
                      Runnable onSuccessCallback, Consumer<Exception> onErrorCallback) {
        this.sourceFile = sourceFile;
        this.targetPath = targetPath;
        this.fileRow = fileRow;
        this.onSuccessCallback = onSuccessCallback;
        this.onErrorCallback = onErrorCallback;
    }

    @Override
    public void run() {
        // Implement the import logic here
        System.out.println("Importing file: " + sourceFile + " to destination: " + targetPath);
        try {
           Files.copy(sourceFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
            Platform.runLater(() -> {
                //update file row
                fileRow.setPending(false);
                if (onSuccessCallback != null) onSuccessCallback.run();
            });
            System.out.println("Import completed successfully.");
        } catch (IOException e) {
            System.err.println("Import interrupted: " + e);
            Platform.runLater(() -> {
                fileRow.setPending(false);
                // Optionally, you can show an error message to the user
                System.err.println("Failed to import file: " + sourceFile);
                if (onErrorCallback != null) onErrorCallback.accept(e);
            });
        }
    }
}
