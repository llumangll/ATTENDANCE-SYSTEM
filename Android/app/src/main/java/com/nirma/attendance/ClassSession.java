package com.nirma.attendance;

public class ClassSession {
    private Long id;
    private String professorName;
    private String subject;
    private boolean isActive;

    // 1. Empty Constructor (Required for some tools)
    public ClassSession() {}

    // 2. THE MISSING PIECE: The 3-Argument Constructor
    public ClassSession(Long id, String professorName, String subject) {
        this.id = id;
        this.professorName = professorName;
        this.subject = subject;
        this.isActive = true;
    }

    // Getters
    public Long getId() { return id; }
    public String getProfessorName() { return professorName; }
    public String getSubject() { return subject; }
    public boolean isActive() { return isActive; }

    // Helper to show text in the list
    @Override
    public String toString() {
        return subject + " - " + professorName;
    }
}