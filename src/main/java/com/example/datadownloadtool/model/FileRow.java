package com.example.datadownloadtool.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.Getter;

import java.nio.file.Path;

public class FileRow {
    private final SimpleBooleanProperty selected = new SimpleBooleanProperty(false);
    private final SimpleStringProperty name;
    private final SimpleStringProperty createTime;
    private final SimpleStringProperty size;
    private final SimpleStringProperty owner;
    private final SimpleStringProperty groupName;
    @Getter
    private final Path path;
    private final BooleanProperty isPending;

    public FileRow(String name, String createTime, String size, String owner, String groupName, Path path) {
        this.name = new SimpleStringProperty(name);
        this.createTime = new SimpleStringProperty(createTime);
        this.size = new SimpleStringProperty(size);
        this.owner = new SimpleStringProperty(owner);
        this.groupName = new SimpleStringProperty(groupName);
        this.path = path;
        this.isPending = new SimpleBooleanProperty(false); // mặc định là false
    }

    public boolean isSelected() { return selected.get(); }
    public SimpleBooleanProperty selectedProperty() { return selected; }

    public String getName() { return name.get(); }
    public SimpleStringProperty nameProperty() { return name; }
    public BooleanProperty isPendingProperty() {
        return isPending;
    }
    public SimpleStringProperty createTimeProperty() { return createTime; }
    public SimpleStringProperty sizeProperty() { return size; }
    public SimpleStringProperty ownerProperty() { return owner; }
    public SimpleStringProperty groupNameProperty() { return groupName; }
    public void setPending(boolean value) {
        isPending.set(value);
    }
    public boolean isPending() {
        return isPending.get();
    }
}
