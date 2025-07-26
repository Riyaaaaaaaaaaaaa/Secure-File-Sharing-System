package com.securefile.repository;

import com.securefile.model.File;
import com.securefile.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FileRepository extends JpaRepository<File, Long> {
    List<File> findByUser(User user);
    List<File> findByUserOrderByCreatedAtDesc(User user);
    List<File> findByFolder(com.securefile.model.Folder folder);
} 