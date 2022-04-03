package com.dataart.blueprintsmanager.service;

import com.dataart.blueprintsmanager.exceptions.NotFoundCustomApplicationException;
import com.dataart.blueprintsmanager.persistence.entity.Role;
import com.dataart.blueprintsmanager.persistence.entity.RoleEntity;
import com.dataart.blueprintsmanager.persistence.repository.RoleRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
@Transactional(readOnly = true)
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleEntity getById(Long roleId) {
        return roleRepository.findById(roleId).orElseThrow(() -> {
            throw new NotFoundCustomApplicationException(String.format("Role with ID = %d not found", roleId));
        });
    }

    public RoleEntity getByName(Role role) {
        return roleRepository.findByName(role).orElseThrow(() -> {
            throw new NotFoundCustomApplicationException(String.format("Role with Name = %s not found", role.name()));
        });
    }

    public List<RoleEntity> getAll() {
        return roleRepository.findAll();
    }
}
