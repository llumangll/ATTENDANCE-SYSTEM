package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SessionRepository extends JpaRepository<ClassSession, Long> {
    // ⬇️ This is the missing line causing the error!
    List<ClassSession> findByActiveTrue();
}