package com.project.ragqa.ingestion;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextChunkerTest {

    private final TextChunker chunker = new TextChunker(100, 20);

    private static String tokens(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(' ');
            sb.append(String.format("w%04d", i));
        }
        return sb.toString();
    }

    private List<TextChunk> chunkOne(String text) {
        return chunker.chunk(List.of(new ExtractedPage(1, text)));
    }

    @Test
    void shortTextStaysOneChunk() {
        List<TextChunk> chunks = chunkOne("Short text.");
        assertEquals(1, chunks.size());
        assertEquals("Short text.", chunks.get(0).text());
    }

    @Test
    void noPagesGivesNoChunks() {
        assertTrue(chunker.chunk(List.of()).isEmpty());
    }

    @Test
    void noChunkExceedsMaxSize() {
        String text = "Lorem ipsum dolor sit amet. ".repeat(60).trim();
        for (TextChunk c : chunkOne(text)) {
            assertTrue(c.text().length() <= 100, "chunk too long: " + c.text().length());
        }
    }

    @Test
    void prefersParagraphBoundary() {
        String p1 = "alpha ".repeat(10).trim();
        String p2 = "bravo ".repeat(10).trim();

        List<TextChunk> chunks = chunkOne(p1 + "\n\n" + p2);

        assertEquals(2, chunks.size());
        assertEquals(p1, chunks.get(0).text());
        assertTrue(chunks.get(1).text().endsWith(p2));
    }

    @Test
    void prefersSentenceBoundaryWhenNoParagraphBreak() {
        String sentence = "Alpha beta gamma delta epsilon.";
        String text = String.join(" ", sentence, sentence, sentence, sentence, sentence);

        List<TextChunk> chunks = chunkOne(text);

        assertEquals(String.join(" ", sentence, sentence, sentence), chunks.get(0).text());
    }

    @Test
    void nextChunkOverlapsTheEndOfThePreviousOne() {
        List<TextChunk> chunks = chunkOne(tokens(40));

        String firstWordOfSecond = chunks.get(1).text().split(" ")[0];
        assertTrue(chunks.get(0).text().contains(firstWordOfSecond),
                "second chunk should start inside the first chunk's text");
    }

    @Test
    void keepsPageNumbersAndNumbersChunksAcrossPages() {
        List<TextChunk> chunks = chunker.chunk(List.of(
                new ExtractedPage(1, tokens(25)),
                new ExtractedPage(2, tokens(25))));

        assertEquals(List.of(1, 1, 2, 2), chunks.stream().map(TextChunk::page).toList());
        assertEquals(List.of(0, 1, 2, 3), chunks.stream().map(TextChunk::index).toList());
    }

    @Test
    void giantUnbrokenTokenIsHardCutWithoutHanging() {
        List<TextChunk> chunks = chunkOne("x".repeat(1000));

        assertTrue(chunks.size() > 1);
        for (TextChunk c : chunks) {
            assertTrue(c.text().length() <= 100);
        }
    }

    @Test
    void invalidConfigIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new TextChunker(100, 60));
        assertThrows(IllegalArgumentException.class, () -> new TextChunker(10, 0));
    }
}