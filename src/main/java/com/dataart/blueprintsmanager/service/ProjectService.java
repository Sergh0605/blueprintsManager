package com.dataart.blueprintsmanager.service;

import com.dataart.blueprintsmanager.dto.ProjectDto;
import com.dataart.blueprintsmanager.persistence.entity.ProjectEntity;
import com.dataart.blueprintsmanager.persistence.entity.StageEntity;
import com.dataart.blueprintsmanager.persistence.repository.ProjectRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;

    public List<ProjectDto> getAll() {
        List<ProjectEntity> projects = projectRepository.fetchAllTransactional();
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
        return new ProjectDto(getEmpty());
    }

    public ProjectDto save(ProjectDto projectForSave) {
            ProjectEntity projectEntity = projectForSave.updateEntity(getEmpty());
            return new ProjectDto(projectRepository.createTransactional(projectEntity));
    }

    public ProjectDto update(ProjectDto projectForUpdate) {
        return new ProjectDto(projectRepository
                .updateTransactional(projectForUpdate.updateEntity(getById(projectForUpdate.getId()))));
    }

    private ProjectEntity getEmpty(){
        return ProjectEntity.builder()
                .id(null)
                .name("Новый проект")
                .objectName("")
                .objectAddress("")
                .releaseDate(LocalDate.now())
                .volumeNumber(1L)
                .volumeName("")
                .code("NEW")
                .stage(StageEntity.builder().id(1L).build())
                .reassemblyRequired(false)
                .editTime(LocalDateTime.now()).build();
    }
}
