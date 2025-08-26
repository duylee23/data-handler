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

        String createConfigTable = """
                CREATE TABLE IF NOT EXISTS config (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    key TEXT NOT NULL UNIQUE,
                    value TEXT NOT NULL
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


        String createUsersTable = """
    CREATE TABLE IF NOT EXISTS users (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        username TEXT UNIQUE NOT NULL,
        password TEXT NOT NULL,
        role TEXT NOT NULL
    );
""";

        String insertAdmin = """
    INSERT OR IGNORE INTO users (username, password, role) VALUES (
        'admin',
        '$2a$10$lItox2qs2TYKgF83htqa/./Krlqze7dlzU9ULhfEscoe6kM4cafvu',
        'admin'
    );
""";

        try (
                Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement()) {
                stmt.execute(createGroupsTable);
                stmt.execute(createGroupFilesTable);
                stmt.execute(createConfigTable);
                stmt.execute(createUsersTable);
                stmt.execute(insertAdmin);
        }
        catch (
                SQLException e) {
            System.err.println("Database init failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
