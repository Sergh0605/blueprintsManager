package com.dataart.blueprintsmanager.persistence.repository;

import com.dataart.blueprintsmanager.persistence.entity.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {
    List<CompanyEntity> findAllByDeletedOrderByName(Boolean deleted);

    Boolean existsByName(String name);
}
