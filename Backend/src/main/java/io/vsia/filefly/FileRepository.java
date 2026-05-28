package io.vsia.filefly;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Das Repository-Interface
public interface FileRepository extends JpaRepository<FileMetadata, Long> {
    Optional<FileMetadata> findByShareId(String shareId);
}
