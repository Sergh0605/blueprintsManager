package com.dataart.blueprintsmanager.service;

import com.dataart.blueprintsmanager.dto.UserDto;
import com.dataart.blueprintsmanager.persistence.entity.UserEntity;
import com.dataart.blueprintsmanager.persistence.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserDto> fetchAll() {
        List<UserEntity> userEntities = userRepository.findAll();
        return toDtoListConverter(userEntities);
    }

    public List<UserDto> fetchAllByCompanyId(Long id) {
        List<UserEntity> userEntities = userRepository.findAllByCompanyId(id);
        return toDtoListConverter(userEntities);
    }

    private List<UserDto> toDtoListConverter(List<UserEntity> userEntities) {
        return userEntities.stream().
                filter(Objects::nonNull).
                map(UserDto::new).
                collect(Collectors.toList());
    }
}