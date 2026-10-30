package com.project.ragqa.ingestion;

import com.project.ragqa.exception.DocumentProcessingException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextExtractorTest {

    private final TextExtractor extractor = new TextExtractor();

    private static byte[] pdfWithPages(String... texts) throws IOException {
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            for (String text : texts) {
                PDPage page = new PDPage();
                doc.addPage(page);
                if (!text.isEmpty()) {
                    try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                        cs.beginText();
                        cs.setFont(font, 12);
                        cs.newLineAtOffset(50, 700);
                        cs.showText(text);
                        cs.endText();
                    }
                }
            }
            doc.save(out);
            return out.toByteArray();
        }
    }

    @Test
    void extractsTextPerPageWithPageNumbers() throws IOException {
        ExtractionResult result = extractor.extract(pdfWithPages("Hello page one", "Second page text"), "application/pdf");

        assertEquals(2, result.pageCount());
        assertEquals(1, result.pages().get(0).page());
        assertTrue(result.pages().get(0).text().contains("Hello page one"));
        assertEquals(2, result.pages().get(1).page());
        assertTrue(result.pages().get(1).text().contains("Second page text"));
    }

    @Test
    void blankPagesAreSkippedButStillCounted() throws IOException {
        ExtractionResult result = extractor.extract(pdfWithPages("First", "", "Third"), "application/pdf");

        assertEquals(3, result.pageCount());
        assertEquals(2, result.pages().size());
        assertEquals(3, result.pages().get(1).page());   // page number survives the gap
    }

    @Test
    void pdfWithNoTextIsRejected() throws IOException {
        byte[] scanLike = pdfWithPages("");
        assertThrows(DocumentProcessingException.class, () -> extractor.extract(scanLike, "application/pdf"));
    }

    @Test
    void corruptPdfIsRejected() {
        byte[] garbage = "this is not a pdf".getBytes(StandardCharsets.UTF_8);
        assertThrows(DocumentProcessingException.class, () -> extractor.extract(garbage, "application/pdf"));
    }

    @Test
    void plainTextIsCleanedAndHasNoPageNumber() {
        byte[] txt = "Hello\u0000 world\r\n\r\n\r\n\r\nBye".getBytes(StandardCharsets.UTF_8);

        ExtractionResult result = extractor.extract(txt, "text/plain");

        assertEquals(1, result.pages().size());
        assertNull(result.pages().get(0).page());
        assertEquals("Hello world\n\nBye", result.pages().get(0).text());
    }

    @Test
    void unsupportedTypeIsRejected() {
        assertThrows(DocumentProcessingException.class, () -> extractor.extract(new byte[]{1}, "image/png"));
    }
}