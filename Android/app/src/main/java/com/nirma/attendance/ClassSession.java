package com.nirma.attendance;

public class ClassSession {
    private Long id;
    private String professorName;
    private String subject;
    private String password;
    private double lat; // 🆕 Professor's Latitude
    private double lon; // 🆕 Professor's Longitude

    // Empty constructor for Firebase/JSON
    public ClassSession() {}

    public ClassSession(Long id, String professorName, String subject, String password, double lat, double lon) {
        this.id = id;
        this.professorName = professorName;
        this.subject = subject;
        this.password = password;
        this.lat = lat;
        this.lon = lon;
    }

    public Long getId() { return id; }
    public String getProfessorName() { return professorName; }
    public String getSubject() { return subject; }
    public String getPassword() { return password; }
    public double getLat() { return lat; }
    public double getLon() { return lon; }
}