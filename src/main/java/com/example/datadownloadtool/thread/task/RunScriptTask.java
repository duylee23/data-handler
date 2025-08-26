package com.example.datadownloadtool.thread.task;

import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Slf4j
public class RunScriptTask implements ScriptTask{
    private final File groupFolder;
    private final ProgressBar progressBar;
    private final String scriptPath; // absolute path to .py
    private final String outputPath; // output path
    private final Consumer<Boolean> onFinish;
    private Process process;

    public RunScriptTask(File groupFolder, ProgressBar progressBar, String scriptPath, String outputPath, Consumer<Boolean> onFinish) {
        this.groupFolder = groupFolder;
        this.progressBar = progressBar;
        this.scriptPath = scriptPath;
        this.outputPath = outputPath;
        this.onFinish = onFinish;
    }

    @Override
    public void run() {
        long totalJsonFile = 0;
        try {
            totalJsonFile = Files.walk(groupFolder.toPath())
                    .filter(p -> p.toString().toLowerCase().endsWith(".json"))
                    .count();
        } catch (IOException e) {
            log.error("Failed to count json files in group: {}", groupFolder.getName(), e);
        }

        try{
            System.out.println("Running Python script for group: " + groupFolder.getName());

            String fixedScriptPath = scriptPath.replace(File.separator, "/");
            String inputPath = groupFolder.getAbsolutePath().replace(File.separator, "/");
            String fixedOutputPath = outputPath.replace(File.separator, "/");

            ProcessBuilder pb = new ProcessBuilder("python", fixedScriptPath, "--input" ,inputPath, "--output" ,fixedOutputPath);

            pb.redirectErrorStream(true);
            Process process = pb.start();
            int pid = (int) process.pid();
            System.out.println("🔍 Script Python started with PID: " + pid);

            final long totalJsonFileFinal = totalJsonFile;
            ScheduledExecutorService scriptProgressScheduler = Executors.newSingleThreadScheduledExecutor();
            AtomicInteger lastLogged = new AtomicInteger(0); // ✅ nhớ lần cuối đã log bao nhiêu ảnh

            Path finalOutputPath = Paths.get(outputPath);
            String groupName = groupFolder.getName();

            scriptProgressScheduler.scheduleAtFixedRate(() -> {
                try(Stream<Path> stream = Files.walk(finalOutputPath)){
                    Optional<Path> match = stream
                            .filter(Files::isDirectory)
                            .filter(p -> p.getFileName().toString().equals(groupName))
                            .findFirst();
                    if(match.isEmpty()) return;
                    Path imageRoot = match.get();
                    long done = Files.walk(imageRoot)
                            .filter(p -> {
                                String name = p.getFileName().toString().toLowerCase();
                                return name.endsWith(".jpg") || name.endsWith("png");
                            }).count();
                    if(done > lastLogged.get()) {
                        double percent = ((double) done / totalJsonFileFinal) * 100.0;
                        double progress =((double) done / totalJsonFileFinal);
                        Platform.runLater(() -> progressBar.setProgress(progress));
                        System.out.printf("📦 Progress: %d/%d (%.2f%%)%n", done, totalJsonFileFinal, percent);
                        lastLogged.set((int) done);
                    }
                } catch (IOException e){
                    System.err.println("❌ Failed to count progress images: " + e.getMessage());
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
                onFinish.accept(exitCode == 0);
            });
        } catch(IOException | InterruptedException e) {
            System.err.println("Failed to run script: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stopScript() {
        if (process != null && process.isAlive()) {
            System.out.println("🛑 Killing Python script: " + groupFolder.getName());
            process.destroy();
        }
    }

    @Override
    public String getGroupName() {
        return "";
    }

    @Override
    public boolean isAlive() {
        return false;
    }
}
