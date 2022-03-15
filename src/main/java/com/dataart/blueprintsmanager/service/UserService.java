package com.dataart.blueprintsmanager.service;

import com.dataart.blueprintsmanager.exceptions.NotFoundCustomApplicationException;
import com.dataart.blueprintsmanager.persistence.entity.UserEntity;
import com.dataart.blueprintsmanager.persistence.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<UserEntity> getAllByPage(Pageable pageable) {
        return userRepository.findAllByDeletedOrderByCompany(false, pageable);
    }

    @Transactional(readOnly = true)
    public UserEntity getById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> {
            throw new NotFoundCustomApplicationException(String.format("User with ID = %d not found", userId));
        });
    }

    @Transactional(readOnly = true)
    public UserEntity getByIdAndCompanyId(Long userId, Long companyId) {
        return userRepository.findByIdAndCompanyId(userId, companyId).orElseThrow(() -> {
            throw new NotFoundCustomApplicationException(String.format("User with ID = %d not found for Company with ID = %d", userId, companyId));
        });
    }

    @Transactional(readOnly = true)
    public List<UserEntity> getAllByCompanyId(@NotNull Long companyId) {
        List<UserEntity> userEntities = new ArrayList<>();
        // TODO: 04.03.2022 Do we need null check here?
            userEntities = userRepository.findAllByCompanyIdOrderByLastName(companyId);
        return userEntities;
    }

    @Transactional(readOnly = true)
    public UserEntity getByLogin(String login) {
        return userRepository.findByLogin(login).orElseThrow(() -> {
            throw new NotFoundCustomApplicationException(String.format("User with login %s not found", login));
        });
    }

    @Transactional
    public UserEntity setDeletedById(Long id, Boolean deletedStatus) {
        UserEntity userForChangeDeleteStatus = getById(id);
        userForChangeDeleteStatus.setDeleted(deletedStatus);
        return userRepository.save(userForChangeDeleteStatus);
    }
}
