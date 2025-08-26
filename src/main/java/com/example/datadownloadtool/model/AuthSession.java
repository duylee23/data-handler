package com.example.datadownloadtool.model;


import lombok.Getter;
import lombok.Setter;

public class AuthSession {
    @Getter
    @Setter
    private static String token;
    @Getter
    @Setter
    private static String username;

    @Getter
    @Setter
    private static String role;



    public static void clear() {
        token = null;
        username = null;
    }
}
