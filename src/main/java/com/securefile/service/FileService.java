package com.securefile.service;

import com.securefile.model.File;
import com.securefile.model.User;
import com.securefile.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final EncryptionService encryptionService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public File getFileById(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));
    }

    public File uploadFile(MultipartFile multipartFile, User user, com.securefile.model.Folder folder) throws IOException {
        String originalFileName = multipartFile.getOriginalFilename();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + fileExtension;
        
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);
        byte[] fileContent = multipartFile.getBytes();
        try {
            byte[] encryptedContent = encryptionService.encrypt(fileContent);
            Files.write(filePath, encryptedContent);

            File file = new File();
            file.setFileName(fileName);
            file.setOriginalFileName(originalFileName);
            file.setFileType(multipartFile.getContentType());
            file.setFileSize(multipartFile.getSize());
            file.setFilePath(filePath.toString());
            file.setUser(user);
            file.setEncrypted(true);
            file.setFolder(folder);

            return fileRepository.save(file);
        } catch (Exception e) {
            // Clean up the file if encryption fails
            Files.deleteIfExists(filePath);
            throw new RuntimeException("Failed to encrypt file: " + e.getMessage());
        }
    }

    public File uploadFile(MultipartFile multipartFile, User user) throws IOException {
        String originalFileName = multipartFile.getOriginalFilename();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + fileExtension;
        
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);
        byte[] fileContent = multipartFile.getBytes();
        try {
            byte[] encryptedContent = encryptionService.encrypt(fileContent);
            Files.write(filePath, encryptedContent);

            File file = new File();
            file.setFileName(fileName);
            file.setOriginalFileName(originalFileName);
            file.setFileType(multipartFile.getContentType());
            file.setFileSize(multipartFile.getSize());
            file.setFilePath(filePath.toString());
            file.setUser(user);
            file.setEncrypted(true);

            return fileRepository.save(file);
        } catch (Exception e) {
            // Clean up the file if encryption fails
            Files.deleteIfExists(filePath);
            throw new RuntimeException("Failed to encrypt file: " + e.getMessage());
        }
    }

    public byte[] downloadFile(Long fileId, User user) throws IOException {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!file.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to file");
        }

        Path filePath = Paths.get(file.getFilePath());
        byte[] encryptedContent = Files.readAllBytes(filePath);
        try {
            return encryptionService.decrypt(encryptedContent);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt file: " + e.getMessage());
        }
    }

    public List<File> getUserFiles(User user) {
        return fileRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public void deleteFile(Long fileId, User user) throws IOException {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!file.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to file");
        }

        Path filePath = Paths.get(file.getFilePath());
        Files.deleteIfExists(filePath);
        fileRepository.delete(file);
    }

    public void moveFilesToRoot(com.securefile.model.Folder folder) {
        List<File> files = fileRepository.findByFolder(folder);
        for (File file : files) {
            file.setFolder(null);
            file.setUpdatedAt(LocalDateTime.now());
        }
        fileRepository.saveAll(files);
    }
} 