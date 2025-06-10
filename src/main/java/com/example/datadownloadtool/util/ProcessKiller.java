package com.example.datadownloadtool.util;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ProcessKiller {

    public static boolean isProcessRunning(int pid) {
        try {
            Process process = new ProcessBuilder("cmd", "/c", "tasklist /FI \"PID eq " + pid + "\"")
                    .redirectErrorStream(true)
                    .start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(String.valueOf(pid))) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error while checking process: " + e.getMessage());
        }
        return false;
    }

    public static boolean killProcess(int pid) {
        try {
            Process process = new ProcessBuilder("cmd", "/c", "taskkill /PID " + pid + " /F")
                    .redirectErrorStream(true)
                    .start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[KILL] " + line);
                }
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error while killing process: " + e.getMessage());
            return false;
        }
    }
}
