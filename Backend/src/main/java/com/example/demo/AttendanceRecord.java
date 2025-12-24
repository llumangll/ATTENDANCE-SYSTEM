package com.example.demo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class AttendanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String studentId; // e.g. "21BCE000"
    private Long sessionId;   // Which class they attended
    private LocalDateTime timestamp;

    // Constructors
    public AttendanceRecord() {}
    
    public AttendanceRecord(String studentId, Long sessionId) {
        this.studentId = studentId;
        this.sessionId = sessionId;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public LocalDateTime getTimestamp() { return timestamp; }
}