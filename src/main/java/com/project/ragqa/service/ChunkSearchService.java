package com.project.ragqa.service;

import com.project.ragqa.dto.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ChunkSearchService {

    private static final Logger log = LoggerFactory.getLogger(ChunkSearchService.class);
    private static final int DEFAULT_TOP_K = 6;

    private final VectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;

    public ChunkSearchService(VectorStore vectorStore,
                              JdbcTemplate jdbcTemplate,
                              TransactionTemplate transactionTemplate) {
        this.vectorStore = vectorStore;
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
    }

    public List<SearchHit> search(String query, UUID ownerId, UUID documentId, Integer topK) {
        int k = (topK == null) ? DEFAULT_TOP_K : topK;

        FilterExpressionBuilder b = new FilterExpressionBuilder();
        Filter.Expression filter = (documentId == null
                ? b.eq("ownerId", ownerId.toString())
                : b.and(b.eq("ownerId", ownerId.toString()), b.eq("documentId", documentId.toString())))
                .build();

        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(k)
                .filterExpression(filter)
                .build();

        List<Document> results = transactionTemplate.execute(status -> {
            jdbcTemplate.execute("SET LOCAL hnsw.iterative_scan = strict_order");
            return vectorStore.similaritySearch(request);
        });

        return results.stream()
                .filter(d -> isOwnedBy(d, ownerId))
                .map(this::toHit)
                .toList();
    }

    private boolean isOwnedBy(Document doc, UUID ownerId) {
        boolean owned = ownerId.toString().equals(doc.getMetadata().get("ownerId"));
        if (!owned) {
            log.warn("Search returned chunk {} not owned by the caller; dropped", doc.getId());
        }
        return owned;
    }

    private SearchHit toHit(Document doc) {
        Map<String, Object> meta = doc.getMetadata();
        Object page = meta.get("page");
        return new SearchHit(
                UUID.fromString(doc.getId()),
                UUID.fromString((String) meta.get("documentId")),
                (String) meta.get("filename"),
                page == null ? null : ((Number) page).intValue(),
                ((Number) meta.get("chunkIndex")).intValue(),
                doc.getScore() == null ? 0.0 : doc.getScore(),
                doc.getText());
    }
}