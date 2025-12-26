package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<AttendanceRecord, Long> {
    
    // 1. Check if this Student already marked (Prevent double marking)
    boolean existsBySessionIdAndStudentId(Long sessionId, String studentId);

    // 2. Check if this Device was already used (Buddy Defense)
    boolean existsBySessionIdAndDeviceId(Long sessionId, String deviceId);
}