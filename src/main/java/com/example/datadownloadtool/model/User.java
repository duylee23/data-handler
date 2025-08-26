package com.example.datadownloadtool.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {
    private int id;
    private String username;
    private String password;
    private String email;
    private String role;

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }
}
