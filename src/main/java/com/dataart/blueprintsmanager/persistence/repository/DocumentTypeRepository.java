package com.dataart.blueprintsmanager.persistence.repository;

import com.dataart.blueprintsmanager.persistence.entity.DocumentType;
import com.dataart.blueprintsmanager.persistence.entity.DocumentTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentTypeRepository  extends JpaRepository<DocumentTypeEntity, Long> {
    Optional<DocumentTypeEntity> findByType(DocumentType type);
    List<DocumentTypeEntity> findAllByUnmodified(Boolean unmodified);
}
