package com.project.ragqa.ingestion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TextChunker {

    private final int maxChars;
    private final int overlapChars;

    public TextChunker(@Value("${app.chunking.max-chars:800}") int maxChars,
                       @Value("${app.chunking.overlap-chars:150}") int overlapChars) {
        if (maxChars < 50 || overlapChars < 0 || overlapChars >= maxChars / 2) {
            throw new IllegalArgumentException(
                    "Invalid chunking config: need maxChars >= 50 and 0 <= overlapChars < maxChars/2");
        }
        this.maxChars = maxChars;
        this.overlapChars = overlapChars;
    }

    public List<TextChunk> chunk(List<ExtractedPage> pages) {
        List<TextChunk> chunks = new ArrayList<>();
        for (ExtractedPage page : pages) {
            for (String piece : split(page.text())) {
                chunks.add(new TextChunk(chunks.size(), page.page(), piece));
            }
        }
        return chunks;
    }

    private List<String> split(String text) {
        List<String> pieces = new ArrayList<>();
        int len = text.length();
        int start = 0;

        while (start < len) {
            int end = (len - start <= maxChars) ? len : findBreak(text, start, start + maxChars);

            String piece = text.substring(start, end).trim();
            if (!piece.isEmpty()) {
                pieces.add(piece);
            }
            if (end >= len) {
                break;
            }
            start = nextStart(text, end);
        }
        return pieces;
    }

    /** Picks where to end a chunk that began at start. limit is start + maxChars and is always < text length. */
    private int findBreak(String text, int start, int limit) {
        int min = start + maxChars / 2;

        int paragraph = text.lastIndexOf("\n\n", limit);
        if (paragraph >= min) {
            return paragraph;
        }

        for (int i = limit - 1; i >= min; i--) {
            char c = text.charAt(i);
            if ((c == '.' || c == '!' || c == '?') && Character.isWhitespace(text.charAt(i + 1))) {
                return i + 1;
            }
        }

        for (int i = limit - 1; i >= min; i--) {
            if (Character.isWhitespace(text.charAt(i))) {
                return i;
            }
        }
        return limit;
    }

    /** The next chunk starts overlapChars before end, moved forward to the start of a word. */
    private int nextStart(String text, int end) {
        int next = end - overlapChars;
        if (!Character.isWhitespace(text.charAt(next - 1))) {
            for (int i = next; i < end; i++) {
                if (Character.isWhitespace(text.charAt(i))) {
                    next = i + 1;
                    break;
                }
            }
        }
        return next;
    }
}