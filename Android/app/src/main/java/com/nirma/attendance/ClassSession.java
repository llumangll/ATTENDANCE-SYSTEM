package com.nirma.attendance;

public class ClassSession {
    private Long id;
    private String professorName;
    private String subject;
    private String password; // 🆕 Added Password

    public ClassSession(Long id, String professorName, String subject, String password) {
        this.id = id;
        this.professorName = professorName;
        this.subject = subject;
        this.password = password;
    }

    public Long getId() { return id; }
    public String getProfessorName() { return professorName; }
    public String getSubject() { return subject; }
    public String getPassword() { return password; } // 🆕 Getter

    @Override
    public String toString() {
        return subject + " (" + professorName + ")";
    }
}