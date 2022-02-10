package com.dataart.blueprintsmanager.service;

import com.dataart.blueprintsmanager.dto.ProjectDto;
import com.dataart.blueprintsmanager.persistence.entity.ProjectEntity;
import com.dataart.blueprintsmanager.persistence.repository.ProjectRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProjectService {
    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<ProjectDto> fetchAll() {
        List<ProjectEntity> projects = projectRepository.findAll();
        return projects.stream().
                filter(Objects::nonNull).
                map(ProjectDto::new).
                collect(Collectors.toList());
    }

    public ProjectDto getById(Long projectId) {
        ProjectEntity project = projectRepository.fetchById(projectId);
        return new ProjectDto(project);
    }

    public ProjectDto getNew() {
        return new ProjectDto(ProjectEntity.getEmpty());
    }
}
