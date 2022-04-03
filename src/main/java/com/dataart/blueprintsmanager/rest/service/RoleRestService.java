package com.dataart.blueprintsmanager.rest.service;

import com.dataart.blueprintsmanager.persistence.entity.RoleEntity;
import com.dataart.blueprintsmanager.persistence.entity.UserEntity;
import com.dataart.blueprintsmanager.rest.dto.RoleDto;
import com.dataart.blueprintsmanager.rest.dto.UserDto;
import com.dataart.blueprintsmanager.rest.mapper.RoleMapper;
import com.dataart.blueprintsmanager.service.RoleService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class RoleRestService {
    private final RoleService roleService;
    private final RoleMapper roleMapper;

    public List<RoleDto> getAllRoles() {
        log.info("Try to get all User Roles");
        List<RoleEntity> roleEntities = roleService.getAll();
        List<RoleDto> roleDtos = roleEntities.stream().map(roleMapper::roleEntityToRoleDto).toList();
        log.info("{} Roles found.", roleDtos.size());
        return roleDtos;
    }
}
