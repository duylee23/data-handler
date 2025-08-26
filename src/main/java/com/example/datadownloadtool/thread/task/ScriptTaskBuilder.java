package com.example.datadownloadtool.thread.task;

import com.example.datadownloadtool.model.GroupRow;

import java.util.function.Consumer;

@FunctionalInterface
public interface ScriptTaskBuilder {
    ScriptTask build(GroupRow groupRow, Consumer<Boolean> callback);
}
