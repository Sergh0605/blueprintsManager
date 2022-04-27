package com.dataart.blueprintsmanager.persistence.repository;

import com.dataart.blueprintsmanager.persistence.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Page<UserEntity> findAllByLoginContainingIgnoreCaseAndDeletedOrderByCompany(String filterLogin, Boolean deleted, Pageable pageable);

    List<UserEntity> findAllByCompanyIdAndDeletedOrderByLastName(Long companyId, Boolean deleted);

    Optional<UserEntity> findByLogin(String login);

    Optional<UserEntity> findByIdAndCompanyId(Long userId, Long companyId);

    Boolean existsByLogin(String login);
}
