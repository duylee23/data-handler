package com.example.datadownloadtool.thread.task;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ProgressUnzipTask implements Runnable{
    private final File zipFile;
    private final File outputDir;
    private final Consumer<Boolean> onFinishCallback;

    public ProgressUnzipTask(File zipFile, File outputDir, Consumer<Boolean> onFinishCallback) {
        this.zipFile = zipFile;
        this.outputDir = outputDir;
        this.onFinishCallback = onFinishCallback;
    }

    @Override
    public void run() {
        List<ZipEntry> entryList = new ArrayList<>();
        //step 1: scan entry to count files
        try( ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while(( entry = zis.getNextEntry()) != null ) {
                //ignore directories to count only files
                if(!entry.isDirectory()){
                    entryList.add(entry);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            finish(false);
            return;
        }

        //step 2: unzip each file with progress
        try( ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while((entry = zis.getNextEntry()) != null) {
                File newFile = createSafeFile(outputDir, entry);
                if(entry.isDirectory()){
                    //if directory, create it
                    newFile.mkdirs();
                    //if not a directory, create parent directories
                } else {
                    newFile.getParentFile().mkdirs();
                    try(FileOutputStream fos = new FileOutputStream(newFile)){
                        byte[] buffer = new byte[1024];
                        int len;
                        while((len = zis.read(buffer))  > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            finish(false);
            return;
        }
        finish(true);
    }

    private void finish(boolean isSuccess) {
        Platform.runLater(() -> {
            if (onFinishCallback != null) {
                onFinishCallback.accept(isSuccess);
            }
        });
    }

    private File createSafeFile(File outputDir, ZipEntry entry) throws IOException{
        File file = new File(outputDir, entry.getName());
        String destDirPath = outputDir.getCanonicalPath();
        String destFilePath = file.getCanonicalPath();
        if(!destFilePath.startsWith(destDirPath + File.separator)){
            throw new IOException("Invalid ZIP entry (potential ZIP Slip: " + entry.getName());
        }
        return file;
    }
}
