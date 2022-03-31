package com.dataart.blueprintsmanager.service;

import com.dataart.blueprintsmanager.exceptions.NotFoundCustomApplicationException;
import com.dataart.blueprintsmanager.persistence.entity.FileEntity;
import com.dataart.blueprintsmanager.persistence.entity.ProjectEntity;
import com.dataart.blueprintsmanager.persistence.entity.ProjectFileEntity;
import com.dataart.blueprintsmanager.persistence.repository.ProjectFileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;

import static com.dataart.blueprintsmanager.util.ResponseUtil.getFile;

@Service
@Slf4j
@Transactional(readOnly = true)
public class ProjectFileService {
    private final String pdfFileNameTemplate;
    private final ProjectFileRepository projectFileRepository;

    public ProjectFileService(@Value("${bpm.project.filename.format}") String pdfFileNameTemplate,
                              ProjectFileRepository projectFileRepository) {
        this.pdfFileNameTemplate = pdfFileNameTemplate;
        this.projectFileRepository = projectFileRepository;
    }

    public Page<ProjectFileEntity> getPdfHistoryForProject(Long projectId, Pageable pageable) {
        return projectFileRepository.findAllByProjectIdOrderByCreationTimeDesc(projectId, pageable);
    }

    @Transactional
    public ProjectFileEntity save(byte[] projectInPdf, ProjectEntity project) {
        ProjectFileEntity newProjectFile = ProjectFileEntity.builder()
                .project(project)
                .projectFile(new FileEntity(null, projectInPdf))
                .build();
        return projectFileRepository.save(newProjectFile);
    }

    public void getProjectFileForDownload(Long projectId, Long projectInPdfId, HttpServletResponse response) {
        ProjectFileEntity projectFile = getByIdAndProjectId(projectInPdfId, projectId);
        getFileInPdf(projectFile, response);
    }

    public void getMostRecentProjectFileForDownload(Long projectId, HttpServletResponse response) {
        ProjectFileEntity projectFile = getNewestByProjectId(projectId);
        getFileInPdf(projectFile, response);
    }

    public void getFileInPdf(ProjectFileEntity projectFile, HttpServletResponse response) {
        byte[] documentInPdf = projectFile.getProjectFile().getFileInBytes();
        String projectFileName = pdfFileNameTemplate.formatted(projectFile.getProject().getCode(),
                projectFile.getProject().getVolumeNumber(),
                projectFile.getProject().getVolumeName());
        getFile(response, documentInPdf, projectFileName);
    }

    private ProjectFileEntity getNewestByProjectId(Long projectId) {
        return projectFileRepository.findFirstByProjectIdOrderByCreationTimeDesc(projectId).orElseThrow(() -> {
            throw new NotFoundCustomApplicationException(String.format("PDF not found for Project with ID = %s", projectId));
        });
    }

    private ProjectFileEntity getByIdAndProjectId(Long projectInPdfId, Long projectId) {
        return projectFileRepository.findByIdAndProjectId(projectInPdfId, projectId).orElseThrow(() -> {
            throw new NotFoundCustomApplicationException(String.format("PDF with ID = %d not found for Project with ID = %s", projectInPdfId, projectId));
        });
    }
}
