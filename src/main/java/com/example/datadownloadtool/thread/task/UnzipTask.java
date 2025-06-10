package com.example.datadownloadtool.thread.task;

import javafx.application.Platform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UnzipTask implements Runnable {
    private File zipFile;
    private File outputDir;
    private Runnable onFinishCallback;

    public UnzipTask(File zipFile, File outputDir, Runnable onFinishCallback) {
        this.zipFile = zipFile;
        this.outputDir = outputDir;
        this.onFinishCallback = onFinishCallback;
    }

    @Override
    public void run() {
        try(ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))){
            ZipEntry entry;
            while((entry = zis.getNextEntry()) != null) {
                File outFile = new File(outputDir, entry.getName());
                if(entry.isDirectory()){
                    outFile.mkdirs();
                } else {
                    outFile.getParentFile().mkdirs();
                    try(FileOutputStream fos = new FileOutputStream(outFile)){
                        byte[] buffer = new byte[1024];
                        int len;
                        while((len = zis.read(buffer)) > 0){
                            fos.write(buffer, 0, len);
                        }
                    }
                }
            }
            zis.closeEntry();
        } catch(IOException e){
            e.printStackTrace();
        } finally {
            if(onFinishCallback != null) {
                Platform.runLater(onFinishCallback);
            }
        }
    }
}
