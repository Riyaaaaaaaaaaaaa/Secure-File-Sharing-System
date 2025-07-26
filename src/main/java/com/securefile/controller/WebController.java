package com.securefile.controller;

import com.securefile.model.File;
import com.securefile.model.User;
import com.securefile.repository.UserRepository;
import com.securefile.service.FileService;
import com.securefile.service.FolderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class WebController {
    private final FileService fileService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FolderService folderService;
    private static final Logger logger = LoggerFactory.getLogger(WebController.class);

    @GetMapping({"/", "/login"})
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String handleRegister(@RequestParam String firstName,
                                 @RequestParam String lastName,
                                 @RequestParam String username,
                                 @RequestParam String email,
                                 @RequestParam String password,
                                 @RequestParam String confirmPassword,
                                 @RequestParam(required = false) String terms,
                                 Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            return "register";
        }
        if (terms == null) {
            model.addAttribute("error", "You must agree to the Terms of Service and Privacy Policy");
            return "register";
        }
        if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
            model.addAttribute("error", "Username or email already exists");
            return "register";
        }
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        user.setRole(com.securefile.model.Role.ROLE_USER);
        userRepository.save(user);
        return "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(value = "folderId", required = false) Long folderId, Principal principal, Model model) {
        try {
            if (principal == null) {
                logger.error("Principal is null");
                return "redirect:/login";
            }
            User user = userRepository.findByUsername(principal.getName()).orElse(null);
            if (user == null) {
                logger.error("User not found for principal: {}", principal.getName());
                return "redirect:/login";
            }
            List<com.securefile.model.Folder> folders = folderService.getUserFolders(user);
            List<File> files;
            if (folderId != null) {
                files = fileService.getUserFiles(user).stream()
                        .filter(f -> f.getFolder() != null && f.getFolder().getId().equals(folderId))
                        .toList();
            } else {
                files = fileService.getUserFiles(user).stream()
                        .filter(f -> f.getFolder() == null)
                        .toList();
            }
            model.addAttribute("folders", folders);
            model.addAttribute("files", files);
            model.addAttribute("selectedFolderId", folderId);
            return "dashboard";
        } catch (Exception e) {
            logger.error("Exception in dashboard: ", e);
            model.addAttribute("error", "Error: " + e.getMessage());
            model.addAttribute("files", List.of());
            model.addAttribute("folders", List.of());
            return "dashboard";
        }
    }

    @GetMapping("/upload")
    public String uploadPage(@RequestParam(value = "folderId", required = false) Long folderId, Model model) {
        model.addAttribute("selectedFolderId", folderId);
        return "upload";
    }

    @PostMapping("/upload")
    public String handleUpload(@RequestParam("file") MultipartFile file,
                              @RequestParam(value = "folderId", required = false) Long folderId,
                              Principal principal, Model model) {
        if (principal == null) return "redirect:/login";
        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) return "redirect:/login";
        com.securefile.model.Folder folder = null;
        if (folderId != null) {
            folder = folderService.getFolderById(folderId).orElse(null);
        }
        try {
            fileService.uploadFile(file, user, folder);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "upload";
        }
        if (folderId != null) {
            return "redirect:/dashboard?folderId=" + folderId;
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<org.springframework.core.io.Resource> downloadFile(@PathVariable Long fileId, Principal principal) throws Exception {
        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) throw new RuntimeException("Unauthorized");
        
        byte[] fileContent = fileService.downloadFile(fileId, user);
        File file = fileService.getFileById(fileId);
        
        org.springframework.core.io.ByteArrayResource resource = new org.springframework.core.io.ByteArrayResource(fileContent);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getOriginalFileName() + "\"")
                .contentType(MediaType.parseMediaType(file.getFileType()))
                .contentLength(fileContent.length)
                .body(resource);
    }

    @GetMapping("/preview/{fileId}")
    public ResponseEntity<org.springframework.core.io.Resource> previewFile(@PathVariable Long fileId, Principal principal) throws Exception {
        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) throw new RuntimeException("Unauthorized");
        
        byte[] fileContent = fileService.downloadFile(fileId, user);
        File file = fileService.getFileById(fileId);
        logger.info("Preview requested: {} (type: {})", file.getOriginalFileName(), file.getFileType());
        
        org.springframework.core.io.ByteArrayResource resource = new org.springframework.core.io.ByteArrayResource(fileContent);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getFileType()))
                .contentLength(fileContent.length)
                .body(resource);
    }

    @GetMapping("/delete/{fileId}")
    public String deleteFile(@PathVariable Long fileId, Principal principal) throws Exception {
        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) return "redirect:/login";
        fileService.deleteFile(fileId, user);
        return "redirect:/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) throws Exception {
        request.logout();
        return "redirect:/login";
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam String username,
                             @RequestParam String password,
                             Model model,
                             HttpServletRequest request) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            model.addAttribute("error", "Invalid username or password.");
            return "login";
        }
        // Authenticate with Spring Security
        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(user.getUsername(), null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authToken);
        return "redirect:/dashboard";
    }

    @PostMapping("/folders/create")
    public String createFolder(@RequestParam String name,
                              @RequestParam(value = "parentFolderId", required = false) Long parentFolderId,
                              Principal principal) {
        if (principal == null) return "redirect:/login";
        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) return "redirect:/login";
        com.securefile.model.Folder parentFolder = null;
        if (parentFolderId != null) {
            parentFolder = folderService.getFolderById(parentFolderId).orElse(null);
        }
        folderService.createFolder(name, user, parentFolder);
        if (parentFolderId != null) {
            return "redirect:/dashboard?folderId=" + parentFolderId;
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/folders/edit")
    public String editFolder(@RequestParam Long folderId,
                           @RequestParam String newName,
                           Principal principal) {
        if (principal == null) return "redirect:/login";
        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) return "redirect:/login";
        
        com.securefile.model.Folder folder = folderService.getFolderById(folderId).orElse(null);
        if (folder == null || !folder.getUser().getId().equals(user.getId())) {
            return "redirect:/dashboard";
        }
        
        folderService.renameFolder(folder, newName);
        return "redirect:/dashboard?folderId=" + folderId;
    }

    @PostMapping("/folders/delete")
    public String deleteFolder(@RequestParam Long folderId,
                             Principal principal) {
        if (principal == null) return "redirect:/login";
        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) return "redirect:/login";
        
        com.securefile.model.Folder folder = folderService.getFolderById(folderId).orElse(null);
        if (folder == null || !folder.getUser().getId().equals(user.getId())) {
            return "redirect:/dashboard";
        }
        
        // Move files to root before deleting folder
        fileService.moveFilesToRoot(folder);
        folderService.deleteFolder(folder);
        return "redirect:/dashboard";
    }
} 