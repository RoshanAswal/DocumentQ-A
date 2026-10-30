package com.project.ragqa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false, updatable = false)
    private UUID ownerId;

    @Column(nullable = false)
    private String filename;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status = DocumentStatus.UPLOADED;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "chunk_count")
    private Integer chunkCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Document() {}

    public Document(UUID ownerId, String filename, String contentType, long sizeBytes) {
        this.ownerId = ownerId;
        this.filename = filename;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public void markProcessing() {
        this.status = DocumentStatus.PROCESSING;
        this.errorMessage = null;
    }

    public void markReady(int pageCount, int chunkCount) {
        this.status = DocumentStatus.READY;
        this.pageCount = pageCount;
        this.chunkCount = chunkCount;
        this.errorMessage = null;
    }

    public void markFailed(String reason) {
        this.status = DocumentStatus.FAILED;
        this.pageCount = null;
        this.chunkCount = null;
        this.errorMessage = reason != null && reason.length() > 500
                ? reason.substring(0, 500)
                : reason;
    }

    public UUID getId() { return id; }
    public UUID getOwnerId() { return ownerId; }
    public String getFilename() { return filename; }
    public String getContentType() { return contentType; }
    public long getSizeBytes() { return sizeBytes; }
    public DocumentStatus getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
    public Integer getPageCount() { return pageCount; }
    public Integer getChunkCount() { return chunkCount; }
    public Instant getCreatedAt() { return createdAt; }
}