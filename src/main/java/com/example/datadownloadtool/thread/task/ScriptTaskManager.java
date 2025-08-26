package com.example.datadownloadtool.thread.task;

import lombok.Getter;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ScriptTaskManager {
    @Getter
    private static final ScriptTaskManager instance = new ScriptTaskManager();

    private final Map<String,ScriptTask> runningTasks = new ConcurrentHashMap<>();

    private ScriptTaskManager() {
    }

    public void register(ScriptTask task) {
        runningTasks.put(task.getGroupName(),task);
    }

    public void unregister(ScriptTask task) {
        runningTasks.remove(task.getGroupName(),task);
    }

    public void stopAll() {
        runningTasks.values().forEach(ScriptTask::stopScript);
        runningTasks.clear();
    }

    public boolean isRunning(String groupName){
        ScriptTask task = runningTasks.get(groupName);
        return task != null && task.isAlive();
    }

    public Collection<String> getRunningGroupNames() {
        return runningTasks.keySet();
    }

    public Collection<ScriptTask> getAllRunningTasks(){
        return runningTasks.values();
    }

}
