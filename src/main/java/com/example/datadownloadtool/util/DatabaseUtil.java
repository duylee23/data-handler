package com.example.datadownloadtool.util;

import com.example.datadownloadtool.repository.DatabaseManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Component
@RequiredArgsConstructor
@Getter
@Setter
public class DatabaseUtil {
    public static void initDatabase() {
        String createGroupsTable = """
            CREATE TABLE IF NOT EXISTS groups (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                type TEXT,
                status TEXT,
                created_at TEXT,
                completed_at TEXT
            );
        """;

        String createGroupFilesTable = """
            CREATE TABLE IF NOT EXISTS group_files (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                group_name TEXT NOT NULL,
                file_name TEXT NOT NULL,
                file_path TEXT NOT NULL,
                added_at TEXT,
                FOREIGN KEY(group_name) REFERENCES groups(name) ON DELETE CASCADE
            );
        """;
        try (
                Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement()) {
                stmt.execute(createGroupsTable);
                stmt.execute(createGroupFilesTable);
            System.out.println("✅ Tables 'groups' and 'group_files' initialized");
        }
        catch (
                SQLException e) {
            e.printStackTrace();
        }
    }
}
