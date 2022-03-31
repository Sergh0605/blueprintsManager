package com.dataart.blueprintsmanager.persistence.repository;

import com.dataart.blueprintsmanager.persistence.entity.DocumentEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends CrudRepository<DocumentEntity, Long> {
    List<DocumentEntity> findByProjectIdOrderByNumberInProject(Long projectId);

    Optional<DocumentEntity> findByIdAndProjectId(Long documentId, Long projectId);
}
