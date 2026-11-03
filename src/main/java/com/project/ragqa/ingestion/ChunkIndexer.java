package com.project.ragqa.ingestion;

import com.project.ragqa.entity.DocumentChunk;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Component;
import org.springframework.ai.vectorstore.filter.Filter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class ChunkIndexer {

    private final VectorStore vectorStore;

    public ChunkIndexer(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void index(List<DocumentChunk> chunks, String filename) {
        if (chunks.isEmpty()) {
            return;
        }
        List<Document> documents = chunks.stream()
                .map(chunk -> toVectorDocument(chunk, filename))
                .toList();
        vectorStore.add(documents);
    }

    private Document toVectorDocument(DocumentChunk chunk, String filename) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("ownerId", chunk.getOwnerId().toString());
        metadata.put("documentId", chunk.getDocumentId().toString());
        metadata.put("chunkIndex", chunk.getChunkIndex());
        metadata.put("filename", filename);
        if (chunk.getPage() != null) {
            metadata.put("page", chunk.getPage());
        }

        return Document.builder()
                .id(chunk.getId().toString())
                .text(chunk.getContent())
                .metadata(metadata)
                .build();
    }

    public void remove(UUID ownerId, UUID documentId) {
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        Filter.Expression filter = b.and(
                b.eq("ownerId", ownerId.toString()),
                b.eq("documentId", documentId.toString())).build();
        vectorStore.delete(filter);
    }
}