package org.example.Entity;

import jakarta.persistence.*;
import org.example.enums.User_Type;

@Entity
@Table(name = "Users")
public class User {
    @Id
    private int id;

    @Column(name = "email" ,nullable = false, unique = true)
    private String email;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private User_Type role;

    @Column(name = "password_hashed", nullable = false)
    private String password;

    public User() {
    }

    public User(int id, String email, String fullName, User_Type role, String password) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.password = password;

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

    public String getPassword() {return password;}

    public void setPassword(String password) {this.password = password;}
}
