package com.example.datadownloadtool.thread.task;

public interface ScriptTask extends Runnable{
    void stopScript();
    String getGroupName();
    boolean isAlive();
}
