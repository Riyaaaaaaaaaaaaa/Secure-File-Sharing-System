package com.securefile.service;

import com.securefile.model.AuditLog;
import com.securefile.model.User;
import com.securefile.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void logEvent(User user, String action, String details, HttpServletRequest request) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUser(user);
        auditLog.setAction(action);
        auditLog.setDetails(details);
        auditLog.setIpAddress(getClientIP(request));
        auditLogRepository.save(auditLog);
    }

    public List<AuditLog> getUserAuditLogs(User user) {
        return auditLogRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc();
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
} 