package com.example.demo; // ✅ Keeps your existing package

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    @Autowired
    private SessionRepository sessionRepository;

    // 1. Start Session (Accepts & Saves Location)
    @PostMapping("/start")
    public ClassSession startSession(
            @RequestParam String professorName,
            @RequestParam String subject,
            @RequestParam String password,
            @RequestParam double lat,
            @RequestParam double lon
    ) {
        ClassSession session = new ClassSession();
        session.setProfessorName(professorName);
        session.setSubject(subject);
        session.setPassword(password);
        session.setLat(lat); 
        session.setLon(lon);
        session.setCreatedAt(LocalDateTime.now());
        session.setActive(true);
        return sessionRepository.save(session);
    }

    // 2. Get Active Sessions
    @GetMapping("/active")
    public List<ClassSession> getActiveSessions() {
        return sessionRepository.findByActiveTrue();
    }

    // 3. Stop Session
    @PostMapping("/stop")
    public void stopSession(@RequestParam Long sessionId) {
        ClassSession session = sessionRepository.findById(sessionId).orElse(null);
        if (session != null) {
            session.setActive(false);
            sessionRepository.save(session);
        }
    }
}