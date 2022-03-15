package com.dataart.blueprintsmanager.persistence.repository;

import com.dataart.blueprintsmanager.persistence.entity.StageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StageRepository extends JpaRepository<StageEntity, Long> {
}
