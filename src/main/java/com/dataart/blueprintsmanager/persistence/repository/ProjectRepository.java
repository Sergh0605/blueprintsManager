package com.dataart.blueprintsmanager.persistence.repository;

import com.dataart.blueprintsmanager.persistence.entity.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.Set;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {
    Boolean existsByCode(String code);
    Set<ProjectEntity> findAllByReassemblyRequiredAndDeleted(Boolean reassemblyRequired, Boolean deleted);
    Page<ProjectEntity> findAllByNameContainingIgnoreCaseAndDeletedOrderByEditTimeDesc(String nameFilter, Boolean deleted, Pageable pageable);
    @Modifying
    @Query("UPDATE ProjectEntity SET reassemblyRequired = ?2 WHERE id = ?1")
    void setReassemblyRequiredById(Long projectId, boolean reassemblyRequired);
    Optional<ProjectEntity> findByIdAndDeleted(Long aLong, Boolean deleted);
}
