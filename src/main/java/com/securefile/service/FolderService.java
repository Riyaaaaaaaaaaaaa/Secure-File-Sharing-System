package com.securefile.service;

import com.securefile.model.Folder;
import com.securefile.model.User;
import com.securefile.repository.FolderRepository;
import com.securefile.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FolderService {
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;

    public Folder createFolder(String name, User user, Folder parentFolder) {
        Folder folder = new Folder();
        folder.setName(name);
        folder.setUser(user);
        folder.setParentFolder(parentFolder);
        return folderRepository.save(folder);
    }

    public List<Folder> getUserFolders(User user) {
        return folderRepository.findByUser(user);
    }

    public List<Folder> getUserFoldersByParent(User user, Folder parentFolder) {
        return folderRepository.findByUserAndParentFolder(user, parentFolder);
    }

    public Optional<Folder> getFolderById(Long id) {
        return folderRepository.findById(id);
    }

    public Folder renameFolder(Long folderId, String newName) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));
        folder.setName(newName);
        folder.setUpdatedAt(LocalDateTime.now());
        return folderRepository.save(folder);
    }

    public void deleteFolder(Long folderId) {
        folderRepository.deleteById(folderId);
    }

    public void renameFolder(Folder folder, String newName) {
        folder.setName(newName);
        folder.setUpdatedAt(LocalDateTime.now());
        folderRepository.save(folder);
    }

    public void deleteFolder(Folder folder) {
        folderRepository.delete(folder);
    }
} 