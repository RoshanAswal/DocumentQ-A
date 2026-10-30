package com.project.ragqa.controller;

import com.project.ragqa.dto.DocumentResponse;
import com.project.ragqa.entity.User;
import com.project.ragqa.service.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> upload(@RequestParam("file") MultipartFile file,
                                                   @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.upload(file, user.getId()));
    }

    @GetMapping
    public List<DocumentResponse> list(@AuthenticationPrincipal User user) {
        return documentService.list(user.getId());
    }

    @GetMapping("/{id}")
    public DocumentResponse get(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return documentService.get(id, user.getId());
    }
}