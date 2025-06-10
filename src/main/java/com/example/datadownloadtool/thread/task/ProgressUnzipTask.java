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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ProgressUnzipTask implements Runnable{
    private final File zipFile;
    private final File outputDir;
    private final Runnable onFinishCallback;

    public ProgressUnzipTask(File zipFile, File outputDir, Runnable onFinishCallback) {
        this.zipFile = zipFile;
        this.outputDir = outputDir;
        this.onFinishCallback = onFinishCallback;
    }

    @Override
    public void run() {
        List<ZipEntry> entryList = new ArrayList<>();
        //step 1: scan entry to count files
        try(ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while(( entry = zis.getNextEntry()) != null){
                //ignore directories to count only files
                if(!entry.isDirectory()){
                    entryList.add(entry);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //step 2: unzip each file with progress
        try(ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while((entry = zis.getNextEntry()) != null) {
                File newFile = new File(outputDir, entry.getName());
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
            throw new RuntimeException(e);
        } finally {
            Platform.runLater(() -> {
                if (onFinishCallback != null) onFinishCallback.run();
            });
        }
    }

    public static void extractRar(File rarFile, File destDir) throws IOException {
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        System.out.println("Rar file start unzipping: " + rarFile.getName());
        try (Archive archive = new Archive(rarFile)) {
            FileHeader header;
            int count = 0;
            while ((header = archive.nextFileHeader()) != null) {
                if (header.isDirectory()) continue;

                String name = header.getFileNameString().trim().replaceAll("\\\\", "/");
                File outFile = new File(destDir, name);
                outFile.getParentFile().mkdirs();

                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    archive.extractFile(header, fos);
                }
                count++;
                System.out.println("Unzip file : " + name);
            }
            System.out.println("Complete all RAR : " + rarFile.getName() + " | Total: " + count + " file");

        } catch (RarException e) {
            throw new RuntimeException(e);
        }
    }
}
