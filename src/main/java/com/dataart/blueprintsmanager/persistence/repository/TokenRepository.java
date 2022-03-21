package com.dataart.blueprintsmanager.persistence.repository;

import com.dataart.blueprintsmanager.persistence.entity.TokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TokenRepository extends JpaRepository<TokenEntity, String> {
    @Modifying
    @Query("UPDATE TokenEntity SET disabled = true WHERE user.id = ?1 AND disabled = false")
    void setDisableByUserId(Long userId);
}
