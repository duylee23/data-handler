package com.example.datadownloadtool.model;

public class Session {
    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static boolean isAdmin() {
        return currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole());
    }
}
