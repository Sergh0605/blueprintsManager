package com.dataart.blueprintsmanager.rest.service;

import com.dataart.blueprintsmanager.aop.track.ParamName;
import com.dataart.blueprintsmanager.aop.track.UserAction;
import com.dataart.blueprintsmanager.aop.track.UserActivityTracker;
import com.dataart.blueprintsmanager.persistence.entity.ProjectEntity;
import com.dataart.blueprintsmanager.persistence.entity.ProjectFileEntity;
import com.dataart.blueprintsmanager.rest.dto.ProjectDto;
import com.dataart.blueprintsmanager.rest.dto.ProjectFileDto;
import com.dataart.blueprintsmanager.rest.mapper.ProjectMapper;
import com.dataart.blueprintsmanager.service.ProjectFileService;
import com.dataart.blueprintsmanager.service.ProjectService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class ProjectRestService {
    private final ProjectService projectService;
    private final ProjectMapper projectMapper;
    private final ProjectFileService projectFileService;

    public Page<ProjectDto> getAllNotDeletedPaginated(Pageable pageable, String search) {
        log.info("Try to get {} page with {} projects", pageable.getPageNumber(), pageable.getPageSize());
        Page<ProjectEntity> projectEntityPage = projectService.getAllNotDeletedPaginated(pageable, search);
        List<ProjectDto> projectDtoList = projectEntityPage.getContent().stream().map(projectMapper::projectEntityToProjectDto).toList();
        Page<ProjectDto> projectDtoPage = new PageImpl<>(projectDtoList, pageable, projectEntityPage.getTotalElements());
        log.info("Page {} with {} projects found", projectDtoPage.getNumber(), projectDtoPage.getNumberOfElements());
        return projectDtoPage;
    }

    public ProjectDto getById(Long projectId) {
        log.info("Try to find project with ID = {}", projectId);
        ProjectEntity project = projectService.getById(projectId);
        ProjectDto projectDto = projectMapper.projectEntityToProjectDto(project);
        log.info("Project with ID = {} found", projectId);
        return projectDto;
    }

    @UserActivityTracker(action = UserAction.UPDATE_PROJECT, projectId = "#projectId.toString()")
    public ProjectDto update(@ParamName("projectId") Long projectId, ProjectDto projectDto) {
        log.info("Try to update project with ID = {}", projectId);
        projectDto.setId(projectId);
        ProjectEntity updatedProject = projectService.update(projectMapper.projectDtoToProjectEntity(projectDto));
        ProjectDto updatedProjectDto = projectMapper.projectEntityToProjectDto(updatedProject);
        log.info("Project with ID = {} updated", projectId);
        return updatedProjectDto;
    }

    @UserActivityTracker(action = UserAction.DELETE_PROJECT, projectId = "#projectId.toString()")
    public void deleteById(@ParamName("projectId") Long projectId) {
        log.info("Try to mark as deleted Project with ID = {}", projectId);
        projectService.setDeleted(projectId, true);
        log.info("Project with ID = {} marked as deleted.", projectId);
    }

    @UserActivityTracker(action = UserAction.RESTORE_PROJECT, projectId = "#projectId.toString()")
    public void restoreById(@ParamName("projectId") Long projectId) {
        log.info("Try to restore Project with ID = {}", projectId);
        projectService.setDeleted(projectId, false);
        log.info("Project with ID = {} restored.", projectId);
    }

    @UserActivityTracker(action = UserAction.CREATE_PROJECT, projectCode = "#project.getCode()")
    public ProjectDto create(@ParamName("project") ProjectDto project) {
        log.info("Try to create new Project with CODE = {}", project.getCode());
        ProjectEntity createdProject = projectService.create(projectMapper.projectDtoToProjectEntity(project));
        ProjectDto createdProjectDto = projectMapper.projectEntityToProjectDto(createdProject);
        log.info("Project with code {} created with ID = {}", createdProjectDto.getCode(), createdProjectDto.getId());
        return createdProjectDto;
    }

    @UserActivityTracker(action = UserAction.DOWNLOAD_PROJECT, projectId = "#projectId.toString()")
    public void getFileForDownload(@ParamName("projectId") Long projectId, HttpServletResponse response) {
        log.info("Try to find project in PDF for download. Project ID = {}", projectId);
        projectFileService.getMostRecentProjectFileForDownload(projectId, response);
        log.info("Project in PDF for download found. Project ID = {}", projectId);
    }

    @UserActivityTracker(action = UserAction.REASSEMBLY_PROJECT, projectId = "#projectId.toString()")
    public ProjectDto assemble(@ParamName("projectId") Long projectId) {
        log.info("Try to assemble Project with ID = {}", projectId);
        ProjectEntity reassembledProject = projectService.reassembleForController(projectId);
        ProjectDto reassembledProjectDto = projectMapper.projectEntityToProjectDto(reassembledProject);
        log.info("Project with ID = {} reassembled", projectId);
        return reassembledProjectDto;
    }


    public Page<ProjectFileDto> getAssembleHistoryPaginated(Long projectId, Pageable pageable) {
        log.info("Try to get {} page with {} pdf files for Project with ID = {}", pageable.getPageNumber(), pageable.getPageSize(), projectId);
        Page<ProjectFileEntity> projectFileEntityPage = projectFileService.getPdfHistoryForProject(projectId, pageable);
        List<ProjectFileDto> projectFileDtoList = projectFileEntityPage.getContent().stream().map(projectMapper::projectFileEntityToDto).toList();
        Page<ProjectFileDto> projectDtoPage = new PageImpl<>(projectFileDtoList, pageable, projectFileEntityPage.getTotalElements());
        log.info("Page {} with {} project files found", projectDtoPage.getNumber(), projectDtoPage.getNumberOfElements());
        return projectDtoPage;
    }

    @UserActivityTracker(action = UserAction.DOWNLOAD_PROJECT, projectId = "#projectId.toString()")
    public void getPdfFromHistoryForDownload(@ParamName("projectId") Long projectId, Long projectInPdfId, HttpServletResponse response) {
        log.info("Try to find project in PDF from History for download. Project ID = {}, history ID = {}", projectId, projectInPdfId);
        projectFileService.getProjectFileForDownload(projectId, projectInPdfId, response);
        log.info("Project in PDF for download found. Project ID = {}", projectId);
    }
}
