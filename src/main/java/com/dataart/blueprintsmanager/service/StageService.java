package com.dataart.blueprintsmanager.service;

import com.dataart.blueprintsmanager.exceptions.NotFoundCustomApplicationException;
import com.dataart.blueprintsmanager.persistence.entity.StageEntity;
import com.dataart.blueprintsmanager.persistence.repository.StageRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
@Transactional(readOnly = true)
public class StageService {
    private final StageRepository stageRepository;

    public List<StageEntity> getAll() {
        return stageRepository.findAll();
    }

    public StageEntity getById(Long stageId) {
        return stageRepository.findById(stageId).orElseThrow(() -> {
            throw new NotFoundCustomApplicationException(String.format("Stage with ID %d not found", stageId));
        });
    }
}
