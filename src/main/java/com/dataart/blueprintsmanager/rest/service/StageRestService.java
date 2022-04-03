package com.dataart.blueprintsmanager.rest.service;

import com.dataart.blueprintsmanager.persistence.entity.RoleEntity;
import com.dataart.blueprintsmanager.persistence.entity.StageEntity;
import com.dataart.blueprintsmanager.rest.dto.RoleDto;
import com.dataart.blueprintsmanager.rest.dto.StageDto;
import com.dataart.blueprintsmanager.rest.mapper.RoleMapper;
import com.dataart.blueprintsmanager.rest.mapper.StageMapper;
import com.dataart.blueprintsmanager.service.RoleService;
import com.dataart.blueprintsmanager.service.StageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class StageRestService {

    private final StageService stageService;
    private final StageMapper stageMapper;

    public List<StageDto> getAllStages() {
        log.info("Try to get all Project Stages");
        List<StageEntity> stageEntities = stageService.getAll();
        List<StageDto> stageDtos = stageEntities.stream().map(stageMapper::stageEntityToStageDto).toList();
        log.info("{} Stages found.", stageDtos.size());
        return stageDtos;
    }
}
