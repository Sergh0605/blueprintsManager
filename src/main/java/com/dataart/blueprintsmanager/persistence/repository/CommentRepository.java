package com.dataart.blueprintsmanager.persistence.repository;

import com.dataart.blueprintsmanager.persistence.entity.CommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    Page<CommentEntity> findByProjectIdAndDeletedOrderByPublicationDateTimeDesc(Long projectId, Boolean deleted, Pageable pageable);
    Page<CommentEntity> findByDocumentIdAndProjectIdAndDeletedOrderByPublicationDateTimeDesc(Long documentId, Long projectId, Boolean deleted, Pageable pageable);
    Optional<CommentEntity> findByIdAndProjectId(Long commentId, Long projectId);
}
