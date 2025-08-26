package com.example.datadownloadtool.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.ProgressBar;
import lombok.Getter;

import java.io.File;
import java.nio.file.Path;

@Getter
public class GroupRow {
    private final SimpleBooleanProperty selected = new SimpleBooleanProperty(false);
    private final SimpleStringProperty groupName;
    private final SimpleStringProperty createTime;
    private final SimpleStringProperty size;
    private final SimpleStringProperty owner;
    private final SimpleStringProperty groupType;
    private final SimpleStringProperty status;
    private final SimpleStringProperty completeTime;
    @Getter
    private final ProgressBar progressBar;
    @Getter
    private final Path path;
    @Getter
    private final File groupFolder;
    private final BooleanProperty isPending;

    public GroupRow(String groupName, String createTime, String size, String owner, String groupType, String status, String completeTime, Path path, File groupFolder) {
        this.groupName = new SimpleStringProperty(groupName);
        this.createTime = new SimpleStringProperty(createTime);
        this.size = new SimpleStringProperty(size);
        this.owner = new SimpleStringProperty(owner);
        this.groupType = new SimpleStringProperty(groupType);
        this.status = new SimpleStringProperty(status);
        this.completeTime = new SimpleStringProperty(completeTime);
        this.path = path;
        this.groupFolder = groupFolder;
        this.progressBar = new ProgressBar(0);
        this.progressBar.setPrefWidth(180);
        this.isPending = new SimpleBooleanProperty(false); // mặc định là false

    }

    public void setCompleteTime(String value) { completeTime.set(value); }
    public void setStatus(String value) { status.set(value); }
    public void setIsPending(boolean pending){
        isPending.set(pending);
    }
    public boolean isSelected() { return selected.get(); }
    public String getGroupName() { return groupName.get(); }

    public SimpleBooleanProperty selectedProperty() { return selected; }
    public SimpleStringProperty groupNameProperty() { return groupName; }
    public SimpleStringProperty createTimeProperty() { return createTime; }
    public SimpleStringProperty sizeProperty() { return size; }
    public SimpleStringProperty ownerProperty() { return owner; }
    public SimpleStringProperty groupTypeProperty() { return groupType; }
    public SimpleStringProperty statusProperty() { return status; }
    public SimpleStringProperty completeTimeProperty() { return completeTime; }
    public boolean isPending() {
        return isPending.get();
    }
    public BooleanProperty isPendingProperty() {
        return isPending;
    }
}
