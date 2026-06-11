package io.vsia.filefly;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
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
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileController {

    private final FileRepository repository;
    private final Path root;

    public FileController(FileRepository repository, @Value("${storage.location}") String storageLocation) {
        this.repository = repository;
        this.root = Paths.get(storageLocation);
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new RuntimeException("Konnte Upload-Ordner nicht erstellen!");
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "password", required = false) String password) { // NEU: Passwort Parameter
        try {
            String generatedShareId = java.util.UUID.randomUUID().toString();
            FileMetadata metadata = new FileMetadata(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    generatedShareId
            );

            // NEU: Passwort setzen, falls eines mitgegeben wurde
            if (password != null && !password.isEmpty()) {
                metadata.setPassword(password);
            }

            repository.save(metadata);
            Files.copy(file.getInputStream(), this.root.resolve("file_" + metadata.getShareId()));

            return ResponseEntity.ok(java.util.Map.of(
                    "fileName", metadata.getOriginalFileName(),
                    "shareId", generatedShareId
            ));
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(500).body(java.util.Map.of("error", "Fehler beim Upload: " + e.getMessage()));
        }
    }

    @GetMapping("/info/{shareId}")
    public ResponseEntity<?> getFileInfo(@PathVariable String shareId) {
        return repository.findByShareId(shareId)
                .map(metadata -> {
                    boolean requiresPassword = metadata.getPassword() != null && !metadata.getPassword().isEmpty();
                    return ResponseEntity.ok(java.util.Map.of("requiresPassword", requiresPassword));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/download/{shareId}")
    public ResponseEntity<?> downloadFile(@PathVariable String shareId, @RequestBody(required = false) java.util.Map<String, String> body) {
        try {
            FileMetadata metadata = repository.findByShareId(shareId)
                    .orElseThrow(() -> new RuntimeException("Datei nicht gefunden"));

            if (metadata.getPassword() != null && !metadata.getPassword().isEmpty()) {
                String providedPassword = (body != null) ? body.get("password") : null;
                if (!metadata.getPassword().equals(providedPassword)) {
                    return ResponseEntity.status(401).body(java.util.Map.of("error", "Falsches Passwort"));
                }
            }

            Path file = root.resolve("file_" + metadata.getShareId());
            Resource resource = new UrlResource(file.toUri());

            String contentType = metadata.getContentType();
            if (contentType == null) contentType = "application/octet-stream";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + metadata.getOriginalFileName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}