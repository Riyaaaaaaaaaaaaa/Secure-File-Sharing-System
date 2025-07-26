package com.securefile.controller;

import com.securefile.model.Folder;
import com.securefile.model.User;
import com.securefile.repository.UserRepository;
import com.securefile.service.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
public class FolderController {
    private final FolderService folderService;
    private final UserRepository userRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createFolder(@RequestParam String name,
                                          @RequestParam(required = false) Long parentFolderId,
                                          Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        Folder parentFolder = null;
        if (parentFolderId != null) {
            parentFolder = folderService.getFolderById(parentFolderId).orElse(null);
        }
        Folder folder = folderService.createFolder(name, user, parentFolder);
        return ResponseEntity.ok(folder);
    }

    @GetMapping
    public ResponseEntity<List<Folder>> getUserFolders(Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(folderService.getUserFolders(user));
    }

    @PostMapping("/rename/{folderId}")
    public ResponseEntity<?> renameFolder(@PathVariable Long folderId, @RequestParam String newName, Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        Folder folder = folderService.getFolderById(folderId).orElse(null);
        if (folder == null || !folder.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        folderService.renameFolder(folderId, newName);
        return ResponseEntity.ok(Map.of("message", "Folder renamed successfully"));
    }

    @DeleteMapping("/{folderId}")
    public ResponseEntity<?> deleteFolder(@PathVariable Long folderId, Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        Folder folder = folderService.getFolderById(folderId).orElse(null);
        if (folder == null || !folder.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        folderService.deleteFolder(folderId);
        return ResponseEntity.ok(Map.of("message", "Folder deleted successfully"));
    }
} 