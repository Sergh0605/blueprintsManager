package com.dataart.blueprintsmanager.persistence.repository;

import com.dataart.blueprintsmanager.persistence.entity.DocumentEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends CrudRepository<DocumentEntity, Long> {
    List<DocumentEntity> findByProjectIdOrderByNumberInProject(Long projectId);

    Optional<DocumentEntity> findByIdAndProjectId(Long documentId, Long projectId);

    @Query("SELECT MAX(d.numberInProject) FROM DocumentEntity d WHERE d.project.id = ?1 AND d.deleted = false")
    Optional<Integer> findMaxDocumentNumberInProject(Long projectId);
}
