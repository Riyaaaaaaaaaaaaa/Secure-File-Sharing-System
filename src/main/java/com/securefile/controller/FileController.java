package com.securefile.controller;

import com.securefile.model.File;
import com.securefile.model.User;
import com.securefile.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        try {
            File uploadedFile = fileService.uploadFile(file, user);
            return ResponseEntity.ok(uploadedFile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long fileId,
            @AuthenticationPrincipal User user) {
        try {
            byte[] fileContent = fileService.downloadFile(fileId, user);
            File file = fileService.getFileById(fileId);
            
            ByteArrayResource resource = new ByteArrayResource(fileContent);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getOriginalFileName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(fileContent.length)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<File>> getUserFiles(@AuthenticationPrincipal User user) {
        List<File> files = fileService.getUserFiles(user);
        return ResponseEntity.ok(files);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(
            @PathVariable Long fileId,
            @AuthenticationPrincipal User user) {
        try {
            fileService.deleteFile(fileId, user);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
} 