package com.example.demo;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    private String uid;       // Roll No or Employee ID
    private String name;      // "Umang Rabadiya"
    private String role;      // "STUDENT" or "PROFESSOR"
    private String department;// "CSE"
    private String photoUrl;  // For the profile picture later

    public User() {}

    public User(String uid, String name, String role, String department) {
        this.uid = uid;
        this.name = name;
        this.role = role;
        this.department = department;
    }

    // Getters
    public String getUid() { return uid; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getDepartment() { return department; }
}