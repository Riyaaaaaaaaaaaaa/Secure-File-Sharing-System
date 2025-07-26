package com.securefile.repository;

import com.securefile.model.Folder;
import com.securefile.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    List<Folder> findByUser(User user);
    List<Folder> findByUserAndParentFolder(User user, Folder parentFolder);
} 