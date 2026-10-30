package com.project.ragqa.ingestion;

import com.project.ragqa.exception.DocumentProcessingException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class TextExtractor {

    public ExtractionResult extract(byte[] content, String contentType) {
        return switch (contentType) {
            case "application/pdf" -> extractPdf(content);
            case "text/plain" -> extractPlainText(content);
            default -> throw new DocumentProcessingException("Unsupported file type: " + contentType);
        };
    }

    private ExtractionResult extractPdf(byte[] content) {
        try (PDDocument document = Loader.loadPDF(content)) {
            int pageCount = document.getNumberOfPages();
            PDFTextStripper stripper = new PDFTextStripper();

            List<ExtractedPage> pages = new ArrayList<>();
            for (int p = 1; p <= pageCount; p++) {
                stripper.setStartPage(p);
                stripper.setEndPage(p);
                String text = clean(stripper.getText(document));
                if (!text.isEmpty()) {
                    pages.add(new ExtractedPage(p, text));
                }
            }
            return result(pageCount, pages);
        } catch (IOException e) {
            throw new DocumentProcessingException("Could not read the PDF (it may be corrupted or password-protected)");
        }
    }

    private ExtractionResult extractPlainText(byte[] content) {
        String text = clean(new String(content, StandardCharsets.UTF_8));
        List<ExtractedPage> pages = text.isEmpty() ? List.of() : List.of(new ExtractedPage(null, text));
        return result(1, pages);
    }

    private ExtractionResult result(int pageCount, List<ExtractedPage> pages) {
        if (pages.isEmpty()) {
            throw new DocumentProcessingException(
                    "No extractable text found (scanned documents need OCR, which is not supported)");
        }
        return new ExtractionResult(pageCount, pages);
    }

    private static String clean(String raw) {
        return raw.replace("\u0000", "")
                .replace("\uFEFF", "")
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .replaceAll("[ \\t]+\n", "\n")
                .replaceAll("\n{3,}", "\n\n")
                .trim();
    }
}