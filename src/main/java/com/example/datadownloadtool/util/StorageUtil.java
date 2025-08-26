package com.example.datadownloadtool.util;


import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.prefs.Preferences;

/**
 * Initializes the storage path used by the application.
 * <p>
 * This method performs the following actions:
 * <ul>
 *     <li>Attempts to retrieve the previously saved base directory from system preferences.</li>
 *     <li>If no valid path is found, prompts the user to select a folder via a directory chooser.</li>
 *     <li>Saves the selected path for future runs.</li>
 *     <li>Resolves three subdirectories within the base folder: {@code fileList}, {@code groupList}, and {@code result}.</li>
 *     <li>Checks write permissions to the base directory and attempts to elevate to admin if necessary.</li>
 *     <li>Creates the three subdirectories on disk if they do not already exist.</li>
 * </ul>
 * This method must be called before accessing any of the subdirectory paths via {@code getFileListDir()},
 * {@code getGroupListDir()}, or {@code getResultDir()}.
 *
 * @throws RuntimeException if the user does not select a directory, or if directory creation or permission checks fail.
 */
@Component
@Getter
@Setter
public class StorageUtil {
    private static final String PREF_KEY = "userSelectedStoragePath";
    private static final String DEFAULT_FILE_LIST = "fileList";
    private static final String DEFAULT_GROUP_LIST = "groupList";
    private static final String DEFAULT_RESULT = "result";
    private final Preferences preferences = Preferences.systemNodeForPackage(StorageUtil.class);

    @Getter
    private Path baseDir;
    @Getter
    private Path fileListDir;
    @Getter
    private Path groupListDir;
    @Getter
    private Path resultDir;

    //Public static method để lấy instance
    //Singleton Instance
    @Getter
    private static final StorageUtil instance = new StorageUtil();
    private static final String CONFIG_FILE = "storage_path.txt";

    //Constructor private để ngăn tạo từ bên ngoài
    private StorageUtil(){}


    // ✅ Gọi một lần ở đâu đó trong app sau khi mở UI
    public void initStoragePath(Window ownerWindow) {
        File configFile = new File(System.getProperty("user.home"), CONFIG_FILE);
        String savedPath = null;
        if(configFile.exists()) {
            try {
                savedPath = Files.readString(configFile.toPath()).trim();
            } catch (IOException e ) {
                e.printStackTrace();
            }
        }

        if(savedPath == null || !Files.exists(Paths.get(savedPath))){
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Please choose the folder to handle data (e.g., C:\\MyAppData)");
            File selected = chooser.showDialog(ownerWindow);

            if(selected == null) {
                throw new RuntimeException("The storage directory has not been chosen");
            }
            savedPath = selected.getAbsolutePath();
            try {
                Files.writeString(configFile.toPath(), savedPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        baseDir = Paths.get(savedPath);
        //tạo đường dẫn path
        fileListDir = baseDir.resolve(DEFAULT_FILE_LIST);
        groupListDir = baseDir.resolve(DEFAULT_GROUP_LIST);
        resultDir = baseDir.resolve(DEFAULT_RESULT);

        //check writing privilege
        if(!canWriteToPath(baseDir)) {
            try{
                String jarPath = new File(StorageUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getAbsolutePath();
                requestAdminAndReturn(jarPath);
            } catch(IOException e) {
                throw new RuntimeException("Permission denied and cannot elevate to admin.", e);
            }
        }
        try{
            // tạo thư mục vật lý
            Files.createDirectories(fileListDir);
            Files.createDirectories(groupListDir);
            Files.createDirectories(resultDir);
        }
        catch (IOException e) {
           throw new RuntimeException("Cannot create storage folder",e);
        }
    }

    public void resetPath() {
        preferences.remove(PREF_KEY);
    }


    private boolean canWriteToPath(Path path) {
        try{
            Path tempFile = Files.createTempFile(path, "test",".tmp");
            System.out.println("created temp file ");
            Files.delete(tempFile);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    //re-launch with admin privilege
    private void requestAdminAndReturn(String jarPath) throws IOException{
        String command = String.format(
                "powershell -Command \"Start-Process java -ArgumentList '-jar \"%s\"' -Verb runAs\"",
                jarPath
        );
        Runtime.getRuntime().exec(command);
        System.exit(0);
    }
}
