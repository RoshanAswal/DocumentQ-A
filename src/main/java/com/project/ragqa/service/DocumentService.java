package com.project.ragqa.service;

import com.project.ragqa.dto.DocumentResponse;
import com.project.ragqa.entity.Document;
import com.project.ragqa.entity.DocumentChunk;
import com.project.ragqa.exception.DocumentProcessingException;
import com.project.ragqa.exception.InvalidFileException;
import com.project.ragqa.exception.ResourceNotFoundException;
import com.project.ragqa.ingestion.ExtractionResult;
import com.project.ragqa.ingestion.TextChunk;
import com.project.ragqa.ingestion.TextChunker;
import com.project.ragqa.ingestion.TextExtractor;
import com.project.ragqa.repository.DocumentChunkRepository;
import com.project.ragqa.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);
    private static final Set<String> ALLOWED_TYPES = Set.of("application/pdf", "text/plain");
    private static final byte[] PDF_MAGIC = "%PDF-".getBytes(StandardCharsets.US_ASCII);

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;
    private final TextExtractor extractor;
    private final TextChunker chunker;
    private final TransactionTemplate transactionTemplate;
    private final long maxBytes;

    public DocumentService(DocumentRepository documentRepository,
                           DocumentChunkRepository chunkRepository,
                           TextExtractor extractor,
                           TextChunker chunker,
                           TransactionTemplate transactionTemplate,
                           @Value("${app.upload.max-bytes}") long maxBytes) {
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.extractor = extractor;
        this.chunker = chunker;
        this.transactionTemplate = transactionTemplate;
        this.maxBytes = maxBytes;
    }

    public DocumentResponse upload(MultipartFile file, UUID ownerId) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is empty");
        }
        if (file.getSize() > maxBytes) {
            throw new InvalidFileException("File is too large (max " + maxBytes / (1024 * 1024) + " MB)");
        }
        String contentType = normalizeType(file.getContentType());
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new InvalidFileException("Only PDF and plain text files are supported");
        }
        byte[] content = readBytes(file);
        if (contentType.equals("application/pdf") && !looksLikePdf(content)) {
            throw new InvalidFileException("File is not a valid PDF");
        }

        Document doc = documentRepository.save(
                new Document(ownerId, sanitizeFilename(file.getOriginalFilename()), contentType, content.length));

        try {
            doc.markProcessing();
            doc = documentRepository.save(doc);

            ExtractionResult extraction = extractor.extract(content, contentType);
            List<TextChunk> chunks = chunker.chunk(extraction.pages());
            doc = storeChunksAndMarkReady(doc, extraction.pageCount(), chunks);
        } catch (DocumentProcessingException e) {
            doc = markFailed(doc, e.getMessage());
        } catch (RuntimeException e) {
            log.error("Unexpected failure while processing document {}", doc.getId(), e);
            doc = markFailed(doc, "Unexpected error while processing the document");
        }
        return DocumentResponse.from(doc);
    }

    public List<DocumentResponse> list(UUID ownerId) {
        return documentRepository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId).stream()
                .map(DocumentResponse::from)
                .toList();
    }

    public DocumentResponse get(UUID id, UUID ownerId) {
        return documentRepository.findByIdAndOwnerId(id, ownerId)
                .map(DocumentResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
    }

    private Document storeChunksAndMarkReady(Document document, int pageCount, List<TextChunk> chunks) {
        List<DocumentChunk> entities = chunks.stream()
                .map(c -> new DocumentChunk(document.getId(), document.getOwnerId(), c.index(), c.page(), c.text()))
                .toList();

        return transactionTemplate.execute(status -> {
            chunkRepository.saveAll(entities);
            document.markReady(pageCount, entities.size());
            return documentRepository.save(document);
        });
    }

    private Document markFailed(Document document, String reason) {
        document.markFailed(reason);
        return documentRepository.save(document);
    }

    private static String normalizeType(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.split(";")[0].trim().toLowerCase(Locale.ROOT);
    }

    private static byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new InvalidFileException("Could not read the uploaded file");
        }
    }

    private static boolean looksLikePdf(byte[] content) {
        if (content.length < PDF_MAGIC.length) {
            return false;
        }
        for (int i = 0; i < PDF_MAGIC.length; i++) {
            if (content[i] != PDF_MAGIC[i]) {
                return false;
            }
        }
        return true;
    }

    private static String sanitizeFilename(String original) {
        if (original == null || original.isBlank()) {
            return "unnamed";
        }
        int lastSeparator = Math.max(original.lastIndexOf('/'), original.lastIndexOf('\\'));
        String name = original.substring(lastSeparator + 1).replaceAll("\\p{Cntrl}", "").trim();
        if (name.isEmpty()) {
            return "unnamed";
        }
        return name.length() > 255 ? name.substring(0, 255) : name;
    }
}