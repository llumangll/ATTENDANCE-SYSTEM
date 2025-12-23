package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*") // Allow Android to access
public class AttendanceController {

    @Autowired private AttendanceRepository attendanceRepo;
    @Autowired private UserRepository userRepo;       // You need to create this Repo file
    @Autowired private SessionRepository sessionRepo; // You need to create this Repo file

    // 1. GET PROFILE (For UI)
    @GetMapping("/api/profile")
    public User getProfile(@RequestParam String uid) {
        return userRepo.findById(uid).orElse(new User("000", "Guest", "GUEST", "N/A"));
    }

    // 2. PROFESSOR: Start Broadcasting
    @GetMapping("/api/session/start")
    public ClassSession startSession(@RequestParam String profName, @RequestParam String subject) {
        ClassSession session = new ClassSession(profName, subject);
        return sessionRepo.save(session);
    }

    // 3. STUDENT: Get List of Broadcasting Classes
    @GetMapping("/api/sessions/active")
    public List<ClassSession> getActiveSessions() {
        // Return only classes where isActive = true
        return sessionRepo.findByIsActiveTrue();
    }

    // 4. MARK ATTENDANCE (Updated to include Session ID)
    @GetMapping("/api/mark")
    public String markAttendance(@RequestParam String uid, @RequestParam Long sessionId) {
        // Simple Logic: Save that 'uid' was present for 'sessionId'
        // (You can expand this logic later)
        System.out.println("Marking: " + uid + " for Session: " + sessionId);
        return "Success";
    }
}