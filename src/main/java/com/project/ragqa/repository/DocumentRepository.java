package com.project.ragqa.repository;

import com.project.ragqa.entity.Document;
import com.project.ragqa.entity.DocumentStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import java.util.Collection;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

    @Transactional
    @Modifying
    @Query("update Document d set d.status = :failed, d.errorMessage = :message where d.status in :stuck")
    int markInterrupted(@Param("failed") DocumentStatus failed,
                        @Param("message") String message,
                        @Param("stuck") Collection<DocumentStatus> stuck);

    Optional<Document> findByIdAndOwnerId(UUID id, UUID ownerId);

    List<Document> findAllByOwnerIdOrderByCreatedAtDesc(UUID ownerId);
}