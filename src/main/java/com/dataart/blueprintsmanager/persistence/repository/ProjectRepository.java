package com.dataart.blueprintsmanager.persistence.repository;

import com.dataart.blueprintsmanager.persistence.entity.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {
    Boolean existsByCode(String code);
    Set<ProjectEntity> findAllByReassemblyRequiredAndDeleted(Boolean reassemblyRequired, Boolean deleted);
    Page<ProjectEntity> findAllByDeletedOrderByEditTimeDesc(Boolean deleted, Pageable pageable);
}
