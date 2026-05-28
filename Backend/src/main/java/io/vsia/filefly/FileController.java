package io.vsia.filefly;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*") // Wichtig für die Verbindung zum Vue-Frontend!
public class FileController {

    private final FileRepository repository;
    private final Path root;

    public FileController(FileRepository repository, @Value("${storage.location}") String storageLocation) {
        this.repository = repository;
        this.root = Paths.get(storageLocation);

        // Ordner beim Start erstellen, falls er nicht existiert
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new RuntimeException("Konnte Upload-Ordner nicht erstellen!");
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String generatedShareId = java.util.UUID.randomUUID().toString();
            FileMetadata metadata = repository.save(new FileMetadata(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    generatedShareId
            ));

            Files.copy(file.getInputStream(), this.root.resolve("file_" + metadata.getShareId()));

            java.util.Map<String, Object> response = java.util.Map.of(
                    "fileName", metadata.getOriginalFileName(),
                    "downloadUrl", "http://localhost:8080/api/files/download/" + metadata.getShareId(),
                    "shareId", generatedShareId
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(500).body(java.util.Map.of("error", "Fehler beim Upload: " + e.getMessage()));
        }
    }

    @GetMapping("/download/{shareId}")
    public ResponseEntity<?> downloadFile(@PathVariable String shareId) {
        try {
            FileMetadata metadata = repository.findByShareId(shareId)
                    .orElseThrow(() -> new RuntimeException("Datei nicht gefunden"));

            Path file = root.resolve("file_" + metadata.getShareId());
            org.springframework.core.io.Resource resource = new UrlResource(file.toUri());

            String contentType = metadata.getContentType();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + metadata.getOriginalFileName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}