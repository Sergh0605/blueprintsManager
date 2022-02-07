package com.dataart.blueprintsmanager.service;

import com.dataart.blueprintsmanager.dto.ProjectDto;
import com.dataart.blueprintsmanager.persistence.entity.ProjectEntity;
import com.dataart.blueprintsmanager.persistence.repository.ProjectRepository;
import com.dataart.blueprintsmanager.util.ProjectUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<ProjectDto> fetchAll(){
        List<ProjectEntity> projects = projectRepository.findAll();
        return ProjectUtil.toDtoListConverter(projects);
    }
}
