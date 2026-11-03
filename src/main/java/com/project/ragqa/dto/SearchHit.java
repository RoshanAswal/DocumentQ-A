package com.project.ragqa.dto;

import java.util.UUID;

public record SearchHit(
        UUID chunkId,
        UUID documentId,
        String filename,
        Integer page,
        int chunkIndex,
        double score,
        String text
) {}