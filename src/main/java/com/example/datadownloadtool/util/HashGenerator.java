package com.example.datadownloadtool.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class HashGenerator {
    public static void main(String[] args) {
        String password = "admin123";
        String hashed = BCrypt.withDefaults().hashToString(10, password.toCharArray());
        System.out.println("✅ Hash: " + hashed);
    }
}
