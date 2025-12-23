package com.example.demo;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class AttendanceController {

    private List<ClassSession> activeSessions = new ArrayList<>();
    
    // Counter to generate IDs manually
    private long idCounter = 1; 

    // 1. CREATE SESSION
    @PostMapping("/create")
    public String createSession(@RequestParam String subject, @RequestParam String professorName) {
        
        // Generate Random 4-Digit OTP
        int randomCode = 1000 + new Random().nextInt(9000);
        String password = String.valueOf(randomCode);

        // Create the Session
        ClassSession session = new ClassSession(professorName, subject, password);
        
        // Manually set the ID so it is not null
        session.setId(idCounter++); 

        activeSessions.add(session);
        
        System.out.println("✅ Session Created: " + subject + " (ID: " + session.getId() + ", Code: " + password + ")");
        
        return password; 
    }

    // 2. GET ACTIVE SESSIONS
    @GetMapping("/sessions/active")
    public List<ClassSession> getActiveSessions() {
        return activeSessions;
    }

    // 3. MARK ATTENDANCE
    @GetMapping("/mark")
    public String markAttendance(@RequestParam String uid, @RequestParam Long sessionId) {
        System.out.println("Present: " + uid + " for Session ID: " + sessionId);
        return "Success";
    }

    // 4. STOP SESSION (New Feature 🛑)
    @PostMapping("/stop")
    public String stopSession(@RequestParam String subject, @RequestParam String professorName) {
        // Removes the class if the Professor Name AND Subject match
        boolean removed = activeSessions.removeIf(session -> 
            session.getProfessorName().equals(professorName) && 
            session.getSubject().equals(subject)
        );
        
        if (removed) {
            System.out.println("🛑 Session Stopped: " + subject);
            return "Stopped";
        } else {
            return "Session not found";
        }
    }
}