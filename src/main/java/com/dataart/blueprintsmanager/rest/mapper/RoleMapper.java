package com.dataart.blueprintsmanager.rest.mapper;

import com.dataart.blueprintsmanager.persistence.entity.RoleEntity;
import com.dataart.blueprintsmanager.rest.dto.RoleDto;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    Set<RoleDto> roleEntitySetToRoleDto(Set<RoleEntity> entity);

    Set<RoleEntity> roleDtoSetToRoleEntity(Set<RoleDto> dto);

    RoleDto roleEntityToRoleDto(RoleEntity entity);

    RoleEntity[] roleDtoArrToRoleEntity(RoleDto[] dtos);

}
