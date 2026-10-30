package com.project.ragqa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "document_chunks")
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "document_id", nullable = false, updatable = false)
    private UUID documentId;

    @Column(name = "owner_id", nullable = false, updatable = false)
    private UUID ownerId;

    @Column(name = "chunk_index", nullable = false, updatable = false)
    private int chunkIndex;

    @Column(updatable = false)
    private Integer page;

    @Column(nullable = false, updatable = false, columnDefinition = "text")
    private String content;

    protected DocumentChunk() {}

    public DocumentChunk(UUID documentId, UUID ownerId, int chunkIndex, Integer page, String content) {
        this.documentId = documentId;
        this.ownerId = ownerId;
        this.chunkIndex = chunkIndex;
        this.page = page;
        this.content = content;
    }

    public UUID getId() { return id; }
    public UUID getDocumentId() { return documentId; }
    public UUID getOwnerId() { return ownerId; }
    public int getChunkIndex() { return chunkIndex; }
    public Integer getPage() { return page; }
    public String getContent() { return content; }
}