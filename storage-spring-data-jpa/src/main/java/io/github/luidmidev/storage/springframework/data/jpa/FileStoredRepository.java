package io.github.luidmidev.storage.springframework.data.jpa;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

public interface FileStoredRepository extends JpaRepository<FileStored, UUID> {

    Optional<FileStored> findByOriginalFileNameAndPath(String originalFileName, String path);

    Optional<FileStoredProjection> findProjectedByOriginalFileNameAndPath(String originalFileName, String path);

    boolean existsByOriginalFileNameAndPath(String originalFileName, String path);

    void deleteByOriginalFileNameAndPath(String filename, String path);

}