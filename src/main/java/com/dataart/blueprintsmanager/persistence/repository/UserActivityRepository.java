package com.dataart.blueprintsmanager.persistence.repository;

import com.dataart.blueprintsmanager.persistence.entity.UserActivityEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserActivityRepository extends JpaRepository<UserActivityEntity, Long> {
    Page<UserActivityEntity> findAllByLoginContainingIgnoreCaseOrMessageContainingIgnoreCaseOrderByTimestampDesc(String loginFilter, String messageFilter, Pageable pageable);
}
