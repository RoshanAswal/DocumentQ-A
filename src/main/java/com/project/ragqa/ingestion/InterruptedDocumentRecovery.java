package com.project.ragqa.ingestion;

import com.project.ragqa.entity.DocumentStatus;
import com.project.ragqa.repository.DocumentRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InterruptedDocumentRecovery {

    private static final Logger log = LoggerFactory.getLogger(InterruptedDocumentRecovery.class);

    private final DocumentRepository documentRepository;

    public InterruptedDocumentRecovery(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @PostConstruct
    void failInterruptedDocuments() {
        int count = documentRepository.markInterrupted(
                DocumentStatus.FAILED,
                "Processing was interrupted by a server restart. Please upload the file again.",
                List.of(DocumentStatus.UPLOADED, DocumentStatus.PROCESSING));
        if (count > 0) {
            log.warn("Marked {} interrupted document(s) as FAILED", count);
        }
    }
}