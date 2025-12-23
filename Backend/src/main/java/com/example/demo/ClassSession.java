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
    
    private String professorName; // "Dr. Sharma"
    private String subject;       // "Advanced Java"
    private boolean isActive;     // true = Broadcasting
    private LocalDateTime startTime;

    public ClassSession() {}

    public ClassSession(String professorName, String subject) {
        this.professorName = professorName;
        this.subject = subject;
        this.isActive = true;
        this.startTime = LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public String getProfessorName() { return professorName; }
    public String getSubject() { return subject; }
    public boolean isActive() { return isActive; }
    
    public void endSession() { this.isActive = false; }
}