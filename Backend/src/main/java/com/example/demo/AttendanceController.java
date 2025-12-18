package com.example.demo;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.HashSet;

@RestController
@RequestMapping("/api")
public class AttendanceController {

    // 1. Stores who marked attendance today (Prevents Duplicates)
    HashSet<String> presentStudents = new HashSet<>();

    // 2. Stores which Device ID belongs to which Student (Prevents Proxies)
    HashMap<String, String> deviceLock = new HashMap<>();

    @GetMapping("/mark")
    public String mark(@RequestParam String uid, @RequestParam String devId) {
        
        // CHECK 1: DUPLICATE ATTENDANCE
        if (presentStudents.contains(uid)) {
            return "Error: You have already marked attendance today.";
        }

        // CHECK 2: PROXY PREVENTION (Device Locking)
        if (deviceLock.containsKey(devId)) {
            String owner = deviceLock.get(devId);
            if (!owner.equals(uid)) {
                return "Error: Proxy Detected! This device belongs to " + owner;
            }
        }

        // IF PASSED: Save Data
        presentStudents.add(uid);
        deviceLock.put(devId, uid);
        
        return "Success: Marked Present";
    }
}