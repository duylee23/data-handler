package com.example.datadownloadtool.dao;

import com.example.datadownloadtool.model.GroupRow;
import com.example.datadownloadtool.repository.DatabaseManager;
import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupDAO {
    public void insertGroup(String name, String type, String status) {
        String sql = "INSERT INTO groups (name, type, status, created_at, completed_at) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, type);
            pstmt.setString(3, status);
            pstmt.setString(4, LocalDateTime.now().toString());
            pstmt.setString(5, ""); // Assuming complete_at is optional and can be null

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to insert group: " + e.getMessage());
        }
    }

    public void insertGroupFile(String groupName, Path filePath) {
        String sql = "INSERT INTO group_files(group_name, file_name, file_path, added_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, groupName);
            pstmt.setString(2, filePath.getFileName().toString());
            pstmt.setString(3, filePath.toString());
            pstmt.setString(4, LocalDateTime.now().toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert file into group_files", e);
        }
    }

    public Map<Path, String> getGroupFileMap() {
        String sql = """
                SELECT gf.file_path, g.name AS group_name
                FROM group_files gf
                JOIN groups g ON gf.group_name = g.name
                """ ;
        Map<Path, String> groupFileMap = new HashMap<>();
        try(Connection conn = DatabaseManager.getConnection();
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(sql)) {
            while (rs.next()){
                Path filePath = Path.of(rs.getString("file_path"));
                String groupName = rs.getString("group_name");
                groupFileMap.put(filePath, groupName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groupFileMap;
    }

    public void deleteFileByPath(Path filePath) {
        String sql = "DELETE FROM group_files WHERE file_path = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, filePath.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to delete file record from group_files: " + e.getMessage());
        }
    }

    public List<GroupRow> getAllGroups () {
        List<GroupRow> groups = new ArrayList<>();
        String sql = "SELECT name, type, status, created_at, completed_at FROM groups";
        try(Connection conn = DatabaseManager.getConnection();
            PreparedStatement pstm = conn.prepareStatement(sql);
            ResultSet rs = pstm.executeQuery()) {
            while(rs.next()){
                String name = rs.getString("name");
                String type = rs.getString("type");
                String status = rs.getString("status");
                String completedAt = rs.getString("completed_at");
                // Dummy/default values
                String createTime = "";
                String size = "";
                String owner = "";
                Path path =  Path.of("");
                GroupRow group = new GroupRow (
                        name,
                        createTime,
                        size,
                        owner,
                        type,
                        status,
                        completedAt,
                        path,
                        path.toFile()
                );
                groups.add(group);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get all groups: " + e.getMessage(), e);
        }
        return groups;
    }

    public String getGroupNameByFilePath(Path filePath) {
        String sql = "SELECT group_name FROM group_files WHERE file_path = ?";
        try(Connection con = DatabaseManager.getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, filePath.toString());
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()) {
                return rs.getString("group_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get group name by file path: " + e.getMessage(), e);
        }
        return null;
    }

    public void updateGroup(String groupName, String status, String completedAt) {
        String sql = "UPDATE groups SET status = ?, completed_at = ? WHERE name = ?";
        try(Connection conn = DatabaseManager.getConnection();
           PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setString(2, completedAt);
            pstmt.setString(3, groupName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error when update group: " + e.getMessage(), e);
        }
    }

    public void deleteGroup(String groupName) {
        String sql = "DELETE FROM groups WHERE name = ?";
        try (Connection connection = DatabaseManager.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(sql)
        ){
            pstmt.setString(1, groupName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error when update group: " + e.getMessage(), e);
        }
    }

    public void deleteGroupFileByGroupName(String groupName) {
        String sql = "DELETE FROM group_files WHERE group_name = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, groupName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error when delete group file: " + e.getMessage(), e);
        }
    }
}
