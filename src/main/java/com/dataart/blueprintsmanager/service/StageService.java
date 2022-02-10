package com.dataart.blueprintsmanager.service;

import com.dataart.blueprintsmanager.dto.StageDto;
import com.dataart.blueprintsmanager.persistence.entity.StageEntity;
import com.dataart.blueprintsmanager.persistence.repository.StageRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class StageService {
    private final StageRepository stageRepository;

    public List<StageDto> getAll() {
        List<StageEntity> stageEntities = stageRepository.findAll();
        return toDtoListConverter(stageEntities);
    }

    private List<StageDto> toDtoListConverter(List<StageEntity> stageEntities) {
        return stageEntities.stream().
                filter(Objects::nonNull).
                map(StageDto::new).
                collect(Collectors.toList());
    }
}
