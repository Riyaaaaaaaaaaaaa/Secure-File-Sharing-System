package com.securefile.controller;

import com.securefile.model.AuditLog;
import com.securefile.model.User;
import com.securefile.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/user")
    public ResponseEntity<List<AuditLog>> getUserAuditLogs(@AuthenticationPrincipal User user) {
        List<AuditLog> logs = auditService.getUserAuditLogs(user);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLog>> getAllAuditLogs() {
        List<AuditLog> logs = auditService.getAllAuditLogs();
        return ResponseEntity.ok(logs);
    }
} 