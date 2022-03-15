package com.dataart.blueprintsmanager.rest.mapper;

import com.dataart.blueprintsmanager.persistence.entity.StageEntity;
import com.dataart.blueprintsmanager.rest.dto.StageDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StageMapper {
    StageDto stageEntityToStageDto(StageEntity entity);

    StageEntity stageDtoToStageEntity(StageDto dto);
}
