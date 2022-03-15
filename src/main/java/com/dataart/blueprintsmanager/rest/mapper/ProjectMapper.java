package com.dataart.blueprintsmanager.rest.mapper;

import com.dataart.blueprintsmanager.persistence.entity.*;
import com.dataart.blueprintsmanager.rest.dto.BasicDto;
import com.dataart.blueprintsmanager.rest.dto.ProjectDto;
import com.dataart.blueprintsmanager.rest.dto.ProjectFileDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    ProjectDto projectEntityToProjectDto(ProjectEntity entity);

    ProjectEntity projectDtoToProjectEntity(ProjectDto dto);

    @Mapping(target = "name", source = "entity.lastName")
    BasicDto UserEntityToBasicDto(UserEntity entity);

    UserEntity BasicDtoToUserEntity(BasicDto dto);

    BasicDto companyEntityToBasicDto(CompanyEntity entity);

    CompanyEntity BasicDtoToCompanyEntity(BasicDto dto);

    BasicDto stageEntityToBasicDto(StageEntity entity);

    StageEntity BasicDtoToStageEntity(BasicDto dto);

    ProjectFileDto projectFileEntityToDto(ProjectFileEntity entity);
}
