package com.securefile.repository;

import com.securefile.model.AuditLog;
import com.securefile.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUserOrderByCreatedAtDesc(User user);
    List<AuditLog> findAllByOrderByCreatedAtDesc();
} 