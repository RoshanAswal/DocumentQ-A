package com.project.ragqa.repository;

import com.project.ragqa.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

    Optional<Document> findByIdAndOwnerId(UUID id, UUID ownerId);

    List<Document> findAllByOwnerIdOrderByCreatedAtDesc(UUID ownerId);
}