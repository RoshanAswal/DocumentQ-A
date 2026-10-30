package com.project.ragqa.ingestion;

import java.util.List;

public record ExtractionResult(int pageCount, List<ExtractedPage> pages) {}