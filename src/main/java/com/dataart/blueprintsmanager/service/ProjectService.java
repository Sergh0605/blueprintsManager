package com.dataart.blueprintsmanager.service;

import com.dataart.blueprintsmanager.dto.DocumentDto;
import com.dataart.blueprintsmanager.dto.ProjectDto;
import com.dataart.blueprintsmanager.pdf.PdfDocumentGenerator;
import com.dataart.blueprintsmanager.persistence.entity.ProjectEntity;
import com.dataart.blueprintsmanager.persistence.entity.StageEntity;
import com.dataart.blueprintsmanager.persistence.repository.ProjectRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private DocumentService documentService;

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

    protected ProjectEntity getById(Long projectId) {
        return projectRepository.fetchByIdTransactional(projectId);
    }

    public ProjectDto getNew() {
        return new ProjectDto(getEmpty());
    }

    public ProjectDto save(ProjectDto projectForSave) {
            ProjectEntity projectEntity = projectForSave.updateEntity(getEmpty());
            ProjectEntity createdProject = projectRepository.createTransactional(projectEntity);
            documentService.createCoverPage(createdProject.getId());
            documentService.createTitlePage(createdProject.getId());
            documentService.createTableOfContents(createdProject.getId());
            reassemble(createdProject.getId());
            return new ProjectDto(projectRepository.fetchByIdTransactional(createdProject.getId()));
    }

    public ProjectDto update(ProjectDto projectForUpdate) {
        return new ProjectDto(projectRepository
                .updateTransactional(projectForUpdate.updateEntity(getById(projectForUpdate.getId()))));
    }

    public ProjectDto reassemble(Long projectId) {
        ProjectEntity project = projectRepository.fetchByIdTransactional(projectId);
        if (project.getReassemblyRequired()) {
            List<byte[]> documentsInBytes = new ArrayList<>();
            List<DocumentDto> documents = documentService.getAllByProjectId(projectId);
            documents.forEach(x -> {
                documentService.reassembleDocument(x.getId());
                documentsInBytes.add(documentService.getInPdfById(x.getId()));
            });
            if (documentsInBytes.size() > 1) {
                byte[] mergedDocument = documentsInBytes.get(0);
                for (int i = 1; i < documentsInBytes.size(); i++) {
                    mergedDocument = PdfDocumentGenerator.mergePdf(mergedDocument, documentsInBytes.get(i));
                }
                project = projectRepository.updateProjectInPdfTransactional(projectId, mergedDocument);
            }
        }
        return new ProjectDto(project);
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

    public ProjectDto getFileForDownload(Long projectId) {
        ProjectEntity project = projectRepository.fetchByIdTransactional(projectId);
        byte[] documentInPdf = projectRepository.fetchProjectInPdfByProjectIdTransactional(projectId);
        String projectFileName = "%s_Том%d_%s.pdf".formatted(project.getCode(), project.getVolumeNumber(), project.getVolumeName());
        return ProjectDto.builder()
                .projectInPdfFileName(projectFileName)
                .projectInPdf(documentInPdf).build();
    }

    public void deleteById(Long projectId) {
        documentService.deleteProject(projectId);
    }
}
