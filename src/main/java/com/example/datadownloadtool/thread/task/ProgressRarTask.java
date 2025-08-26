package com.example.datadownloadtool.thread.task;
import javafx.application.Platform;

import java.io.*;
import java.util.function.Consumer;

public class ProgressRarTask implements Runnable{
    private final File rarFile;
    private final File outputDir;
    private final Consumer<Boolean> onFinishCallback;


    public ProgressRarTask(File rarFile, File outputDir,  Consumer<Boolean> onFinishCallback) {
        this.rarFile = rarFile;
        this.outputDir = outputDir;
        this.onFinishCallback = onFinishCallback;
    }

    @Override
    public void run() {
        boolean isSuccess = false;
        System.out.println("Starting RAR extraction using external command: " + rarFile.getName());
        try {
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            // Gọi lệnh: unrar x -o+ <rarFile> <outputDir>
            ProcessBuilder pb = new ProcessBuilder(
                    "unrar", "x", "-o+",
                    rarFile.getAbsolutePath(),
                    outputDir.getAbsolutePath()
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Ghi log nếu cần
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[unrar] " + line);
                }
            }

            int exitCode = process.waitFor();
            isSuccess = exitCode == 0;
            if (isSuccess) {
                System.out.println("✅ RAR extraction completed: " + rarFile.getName());
            } else {
                System.err.println("❌ RAR extraction failed with exit code: " + exitCode + " for file: " + rarFile.getName());
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("❌ Failed to extract RAR: " + rarFile.getName());
            e.printStackTrace();
        } finally {
            boolean finalSuccess = isSuccess;
            Platform.runLater(() -> {
                System.out.println("🔁 Calling onFinishCallback for RAR: " + rarFile.getName() + " → success = " + finalSuccess);
                if(onFinishCallback != null) {
                    onFinishCallback.accept(finalSuccess);
                }
            });
        }
    }


}
