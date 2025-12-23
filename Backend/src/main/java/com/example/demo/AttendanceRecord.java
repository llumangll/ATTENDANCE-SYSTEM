package com.example.demo;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
public class AttendanceRecord {

    @Id
    private String rollNo;      // The Student ID (Primary Key)
    private String deviceId;    // The Phone's Unique ID
    private LocalDateTime time; // When they marked it

    // Standard Constructors
    public AttendanceRecord() {}

    public AttendanceRecord(String rollNo, String deviceId) {
        this.rollNo = rollNo;
        this.deviceId = deviceId;
        this.time = LocalDateTime.now();
    }

    // Getters
    public String getRollNo() { return rollNo; }
    public String getDeviceId() { return deviceId; }
}