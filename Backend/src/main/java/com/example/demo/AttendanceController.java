package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AttendanceController {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @GetMapping("/mark")
    public String markAttendance(@RequestParam String uid, @RequestParam Long sessionId) {
        // 1. Create the record
        AttendanceRecord record = new AttendanceRecord(uid, sessionId);
        
        // 2. Save to Database
        attendanceRepository.save(record);
        
        return "Marked Present";
    }
}