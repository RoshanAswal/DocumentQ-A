package com.project.ragqa.dto;

import com.project.ragqa.entity.Document;

import java.time.Instant;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        String filename,
        String contentType,
        long sizeBytes,
        String status,
        String errorMessage,
        Integer pageCount,
        Integer chunkCount,
        Instant createdAt
) {
    public static DocumentResponse from(Document d) {
        return new DocumentResponse(
                d.getId(), d.getFilename(), d.getContentType(), d.getSizeBytes(),
                d.getStatus().name(), d.getErrorMessage(),
                d.getPageCount(), d.getChunkCount(), d.getCreatedAt());
    }
}