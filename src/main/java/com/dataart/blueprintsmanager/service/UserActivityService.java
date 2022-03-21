package com.dataart.blueprintsmanager.service;

import com.dataart.blueprintsmanager.persistence.entity.UserActivityEntity;
import com.dataart.blueprintsmanager.persistence.repository.UserActivityRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@AllArgsConstructor
public class UserActivityService {
    private final UserActivityRepository userActivityRepository;

    @Transactional(readOnly = true)
    public Page<UserActivityEntity> getAllPaginated(Pageable pageable) {
        return userActivityRepository.findAll(pageable);
    }

    public UserActivityEntity save(UserActivityEntity userActivityEntity) {
        return userActivityRepository.save(userActivityEntity);
    }
}
