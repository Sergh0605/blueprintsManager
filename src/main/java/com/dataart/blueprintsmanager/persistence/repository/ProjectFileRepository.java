package com.dataart.blueprintsmanager.persistence.repository;

import com.dataart.blueprintsmanager.persistence.entity.ProjectFileEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectFileRepository extends JpaRepository<ProjectFileEntity, Long> {

    Optional<ProjectFileEntity> findFirstByProjectIdOrderByCreationTimeDesc(Long projectId);

    Page<ProjectFileEntity> findAllByProjectIdOrderByCreationTimeDesc(Long projectId, Pageable pageable);

    Optional<ProjectFileEntity> findByIdAndProjectId(Long projectInPdfId, Long projectId);
}
