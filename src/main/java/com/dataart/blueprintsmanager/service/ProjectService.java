package com.dataart.blueprintsmanager.service;

import com.dataart.blueprintsmanager.dto.ProjectDto;
import com.dataart.blueprintsmanager.persistence.entity.ProjectEntity;
import com.dataart.blueprintsmanager.persistence.repository.ProjectRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;

    public List<ProjectDto> getAll() {
        List<ProjectEntity> projects = projectRepository.findAll();
        return projects.stream().
                filter(Objects::nonNull).
                map(ProjectDto::new).
                collect(Collectors.toList());
    }

    public ProjectDto getDtoById(Long projectId) {
        return new ProjectDto(getById(projectId));
    }

    private ProjectEntity getById(Long projectId) {
        return projectRepository.fetchByIdTransactional(projectId);
    }

    public ProjectDto getNew() {
        return new ProjectDto(ProjectEntity.getEmpty());
    }

    public ProjectDto save(ProjectDto projectForSave) {
            ProjectEntity projectEntity = projectForSave.updateEntity(ProjectEntity.getEmpty());
            return new ProjectDto(projectRepository.createTransactional(projectEntity));
    }

    public ProjectDto update(ProjectDto projectForUpdate) {
        return new ProjectDto(projectRepository
                .updateTransactional(projectForUpdate.updateEntity(getById(projectForUpdate.getId()))));
    }
}
