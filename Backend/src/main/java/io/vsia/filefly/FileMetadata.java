package io.vsia.filefly;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class FileMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String originalFileName;
    private String contentType;
    private String shareId;

    public FileMetadata() {}
    public FileMetadata(String originalFileName, String contentType, String shareId) {
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.shareId = shareId;
    }

}

