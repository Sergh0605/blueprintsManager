package com.dataart.blueprintsmanager.service;

import com.dataart.blueprintsmanager.dto.UserDto;
import com.dataart.blueprintsmanager.persistence.entity.UserEntity;
import com.dataart.blueprintsmanager.persistence.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public List<UserDto> getAll() {
        List<UserEntity> userEntities = userRepository.fetchAllTransactional();
        return toDtoListConverter(userEntities);
    }

    public List<UserDto> getAllByCompanyId(Long id) {
        List<UserEntity> userEntities = new ArrayList<>();
        if (id != null) {
            userEntities = userRepository.fetchAllByCompanyId(id);
        }
        return toDtoListConverter(userEntities);
    }

    private List<UserDto> toDtoListConverter(List<UserEntity> userEntities) {
        return userEntities.stream().
                filter(Objects::nonNull).
                map(UserDto::new).
                collect(Collectors.toList());
    }
}
