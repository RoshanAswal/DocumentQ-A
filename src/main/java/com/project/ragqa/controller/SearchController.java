package com.project.ragqa.controller;

import com.project.ragqa.dto.SearchHit;
import com.project.ragqa.dto.SearchQuery;
import com.project.ragqa.entity.User;
import com.project.ragqa.service.ChunkSearchService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final ChunkSearchService searchService;

    public SearchController(ChunkSearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping
    public List<SearchHit> search(@Valid @RequestBody SearchQuery query,
                                  @AuthenticationPrincipal User user) {
        return searchService.search(query.query(), user.getId(), query.documentId(), query.topK());
    }
}