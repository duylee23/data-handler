package com.example.datadownloadtool.thread.executor;

import com.example.datadownloadtool.model.GroupRow;
import com.example.datadownloadtool.thread.task.RunScript2dTldTask;
import com.example.datadownloadtool.thread.task.RunScript3dOdTask;
import javafx.application.Platform;

import java.nio.file.Path;
import java.util.function.Consumer;

public class ScriptTaskFactory {
    public static Runnable createScriptTask(GroupRow group, Consumer<Boolean> callback) {
        String type = group.getGroupType().get();
        Path scriptPath;
        Path resultPath = Path.of("D:/data_download/result");

        switch (type) {
            case "3D":
                scriptPath = Path.of("D:/data_download/script/convert_pcd_3d_visual.py");
                return new RunScript3dOdTask(group.getGroupFolder(), group.getProgressBar(), scriptPath.toString(), resultPath.toString(), callback);
            case "2D":
                scriptPath = Path.of("D:/data_download/script/TLD_visual_20240408_final.py");
                return new RunScript2dTldTask(group.getGroupFolder(), group.getProgressBar(), scriptPath.toString(), resultPath.toString(), callback);
            default:
                return () -> {
                    System.out.println("Unsupported group type: " + type);
                    Platform.runLater(() -> callback.accept(false));
                };
        }
    }
}
