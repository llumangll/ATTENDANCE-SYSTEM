package com.example.demo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class ClassSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String professorName; 
    private String subject;       
    private boolean isActive;     
    private LocalDateTime startTime;
    
    // Stores the 4-digit OTP
    private String password; 

    public ClassSession() {}

    public ClassSession(String professorName, String subject, String password) {
        this.professorName = professorName;
        this.subject = subject;
        this.password = password;
        this.isActive = true;
        this.startTime = LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public String getProfessorName() { return professorName; }
    public String getSubject() { return subject; }
    public boolean isActive() { return isActive; }
    public String getPassword() { return password; }
    
    // 🆕 IMPORTANT FIX: Add this Setter!
    // This allows the Controller to manually assign ID = 1, 2, 3...
    public void setId(Long id) { 
        this.id = id; 
    }

    public void endSession() { this.isActive = false; }
}