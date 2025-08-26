package com.example.datadownloadtool.thread.executor;

import com.example.datadownloadtool.model.GroupRow;
import com.example.datadownloadtool.thread.task.*;
import com.example.datadownloadtool.util.StorageUtil;
import javafx.application.Platform;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Slf4j
public class ScriptTaskFactory {
    private final StorageUtil storageUtil = StorageUtil.getInstance();
    private final Map<String, ScriptTaskBuilder> builders = new HashMap<>();

    {
        builders.put("3D", (group, callback) -> new RunScript3dOdTask(
                group.getGroupFolder(),
                group.getProgressBar(),
                "D:/data_download/script/convert_pcd_3d_visual.py",
                storageUtil.getResultDir().toString(),
                callback
        ));

        builders.put("2D", (group, callback) -> new RunScript2dTldTask(
                group.getGroupFolder(),
                group.getProgressBar(),
                "D:/data_download/script/TLD_visual_20240408_final_edit3.py",
                storageUtil.getResultDir().resolve(group.getGroupFolder().getName()).toString(),
                callback
        ));
    }

    public Runnable createScriptTask(GroupRow group, Consumer<Boolean> callback) {
        String type = group.getGroupType().get();
        ScriptTaskBuilder builder = builders.get(type);
        if (builder == null) {
            return () -> {
                System.out.println("Unsupported group type: " + type);
                Platform.runLater(() -> callback.accept(false));
            };
        }
        // return the exact task based on group type
        ScriptTask task = builder.build(group, callback);
        ScriptTaskManager.getInstance().register(task);
        return task;
    }

}
