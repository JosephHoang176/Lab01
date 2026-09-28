package org.example.Entity;

import org.example.enums.User_Type;

public class User {
    private int id;
    private String email;
    private String fullName;
    private User_Type role;

    public User() {
    }

    public User(int id, String email, String fullName, User_Type role) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public User_Type getRole() {
        return role;
    }

    public void setRole(User_Type role) {
        this.role = role;
    }
}
