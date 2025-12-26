package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AttendanceController {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @GetMapping("/mark")
    public String markAttendance(
            @RequestParam String uid, 
            @RequestParam Long sessionId,
            @RequestParam String deviceId
    ) {
        // 🕵️‍♂️ DEBUG: Print exactly what the server sees
        System.out.println("----- NEW REQUEST -----");
        System.out.println("Student: " + uid);
        System.out.println("Session: " + sessionId);
        System.out.println("DeviceID: " + deviceId);

        // 1. Check: Did THIS STUDENT already mark?
        if (attendanceRepository.existsBySessionIdAndStudentId(sessionId, uid)) {
            System.out.println("Result: Blocked (Student already marked)");
            return "⚠️ You have already marked attendance!";
        }

        // 2. Check: Was THIS PHONE used by someone else?
        boolean deviceUsed = attendanceRepository.existsBySessionIdAndDeviceId(sessionId, deviceId);
        System.out.println("Is Device Used? " + deviceUsed); // Check this line in console!

        if (deviceUsed) {
            System.out.println("Result: Blocked (Proxy)");
            return "❌ Proxy Detected! This device was already used.";
        }

        // 3. Success -> Save
        AttendanceRecord record = new AttendanceRecord(uid, sessionId, deviceId);
        attendanceRepository.save(record);
        System.out.println("Result: Saved Successfully");
        
        return "✅ Marked Present";
    }
}