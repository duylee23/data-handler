package com.example.datadownloadtool.repository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String DB_NAME = "data.db";

    public static Connection getConnection() throws SQLException {
        try {
            // Use a safe writeable location: C:\Users\<User>\AppData\Local\DataDownloadTool\data.db
            String localAppData = System.getenv("LOCALAPPDATA");
            Path dbPath = Paths.get(localAppData, "DataDownloadTool", DB_NAME);
            Files.createDirectories(dbPath.getParent());
            System.out.println("Using DB at: " + dbPath);
            String dbUrl = "jdbc:sqlite:" + dbPath.toString();
            return DriverManager.getConnection(dbUrl);
        } catch (Exception e) {
            throw new RuntimeException("Error initializing database", e);
        }
    }
}