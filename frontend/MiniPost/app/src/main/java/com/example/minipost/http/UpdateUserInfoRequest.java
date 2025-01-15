package com.example.minipost.http;

public class UpdateUserInfoRequest {
    private String userId;
    private String username;
    private String password;
    private String email;

    public UpdateUserInfoRequest(String userId, String username, String password, String email) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }
}