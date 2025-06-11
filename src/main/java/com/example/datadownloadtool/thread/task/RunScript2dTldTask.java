package com.example.datadownloadtool.thread.task;

import javafx.application.Platform;
import javafx.scene.control.ProgressBar;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class RunScript2dTldTask implements Runnable{
    private final File groupFolder;
    private final ProgressBar progressBar;
    private final String scriptPath; // absolute path to .py
    private final String outputPath; // output path
    private final Consumer<Boolean> onFinish;

    public RunScript2dTldTask(File groupFolder, ProgressBar progressBar, String scriptPath, String outputPath, Consumer<Boolean> onFinish) {
        this.groupFolder = groupFolder;
        this.progressBar = progressBar;
        this.scriptPath = scriptPath;
        this.outputPath = outputPath;
        this.onFinish = onFinish;
    }

    @Override
    public void run() {
        try{
            System.out.println("Running Python 2D TLD script for group: " + groupFolder.getName());

            ProcessBuilder pb = new ProcessBuilder("python", scriptPath, groupFolder.getAbsolutePath().replace(File.separator, "/"));
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int pid = (int) process.pid();
            System.out.println("🔍 Script Python 2D TLD started with PID: " + pid);
            // 🔁 Tạo tiến trình giả lập progress từ 0.5 → 1.0
            ScheduledExecutorService scriptProgressScheduler = Executors.newSingleThreadScheduledExecutor();
            AtomicInteger tick = new AtomicInteger(0);
            final int maxTicks = 100;

            scriptProgressScheduler.scheduleAtFixedRate(() -> {
                int current = tick.incrementAndGet();
                double progress = 0.5 + Math.min(0.5, (current / (double) maxTicks) * 0.5);
                Platform.runLater(() -> progressBar.setProgress(progress));

                if (current >= maxTicks) {
                    scriptProgressScheduler.shutdown();
                }
            }, 0, 1, TimeUnit.SECONDS);
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[PYTHON] " + line);
                }
            }
            int exitCode = process.waitFor();
            scriptProgressScheduler.shutdownNow();

            System.out.println("Python script finished with exit code: " + exitCode);
            Platform.runLater(() -> {
                // execute callback when script finished ( exitCode == 0)
                onFinish.accept(exitCode == 0);
            });
        } catch(IOException | InterruptedException e) {
            System.err.println("Failed to run script: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
