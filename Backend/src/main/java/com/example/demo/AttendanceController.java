package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;  // 🆕 Import
import org.springframework.http.ResponseEntity; // 🆕 Import
import org.springframework.web.bind.annotation.*;
import java.util.List; // 🆕 Import

@RestController
@RequestMapping("/api")
public class AttendanceController {

    @Autowired
    private AttendanceRepository attendanceRepository;

    // --- Existing Method: Mark Attendance ---
    @GetMapping("/mark")
    public String markAttendance(
            @RequestParam String uid, 
            @RequestParam Long sessionId,
            @RequestParam String deviceId
    ) {
        System.out.println("----- NEW REQUEST -----");
        System.out.println("Student: " + uid + " | Session: " + sessionId + " | Device: " + deviceId);

        if (attendanceRepository.existsBySessionIdAndStudentId(sessionId, uid)) {
            return "⚠️ You have already marked attendance!";
        }

        if (attendanceRepository.existsBySessionIdAndDeviceId(sessionId, deviceId)) {
            return "❌ Proxy Detected! This device was already used.";
        }

        AttendanceRecord record = new AttendanceRecord(uid, sessionId, deviceId);
        attendanceRepository.save(record);
        System.out.println("Result: Saved Successfully");
        
        return "✅ Marked Present";
    }

    // --- 🆕 NEW METHOD: Export Attendance to CSV ---
    @GetMapping("/attendance/export/{sessionId}")
    public ResponseEntity<String> exportAttendance(@PathVariable Long sessionId) {
        // 1. Fetch data from DB
        List<AttendanceRecord> records = attendanceRepository.findBySessionId(sessionId);
        
        // 2. Build CSV Content
        StringBuilder csv = new StringBuilder();
        csv.append("Student ID,Device ID,Time\n"); // Header
        
        for (AttendanceRecord record : records) {
            csv.append(record.getStudentId()).append(",");
            csv.append(record.getDeviceId()).append(",");
            csv.append(record.getTimestamp()).append("\n");
        }

        // 3. Return as File Download
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance_" + sessionId + ".csv")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                .body(csv.toString());
    }
}