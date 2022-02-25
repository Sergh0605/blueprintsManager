package com.dataart.blueprintsmanager.service;

import com.dataart.blueprintsmanager.dto.DocumentDto;
import com.dataart.blueprintsmanager.dto.ProjectDto;
import com.dataart.blueprintsmanager.email.EmailService;
import com.dataart.blueprintsmanager.pdf.PdfDocumentGenerator;
import com.dataart.blueprintsmanager.persistence.entity.ProjectEntity;
import com.dataart.blueprintsmanager.persistence.entity.StageEntity;
import com.dataart.blueprintsmanager.persistence.repository.ProjectRepository;
import com.dataart.blueprintsmanager.util.CustomPage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.dataart.blueprintsmanager.util.ApplicationUtil.getFile;

@Service
@Slf4j
@AllArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final DocumentService documentService;
    private final UserService userService;
    private final CompanyService companyService;
    private final StageService stageService;
    private final EmailService emailService;

    public CustomPage<ProjectDto> getAllPaginated(Pageable pageable) {
        CustomPage<ProjectEntity> pageOfProjectEntities = projectRepository.fetchAllPaginated(pageable);
        return new CustomPage<>(toDtoList(pageOfProjectEntities.getContent()), pageable, pageOfProjectEntities.getTotal());
    }

    public ProjectDto getDtoById(Long projectId) {
        return new ProjectDto(getById(projectId));
    }

    public ProjectEntity getById(Long projectId) {
        return projectRepository.fetchByIdWrapped(projectId);
    }

    public ProjectDto prepareNewProjectDto() {
        return new ProjectDto(getEmpty());
    }

    public ProjectDto create(ProjectDto projectForSave) {
        String codeForSave = null;
        if (!Objects.equals(projectForSave.getCode(), "")) {
            codeForSave = projectForSave.getCode();
        }
        ProjectEntity projectForCreationEntity = ProjectEntity.builder()
                .name(projectForSave.getName())
                .objectName(projectForSave.getObjectName())
                .objectAddress(projectForSave.getObjectAddress())
                .releaseDate(projectForSave.getReleaseDate())
                .volumeNumber(projectForSave.getVolumeNumber())
                .volumeName(projectForSave.getVolumeName())
                .code(codeForSave)
                .designer(userService.getById(projectForSave.getDesignerId()))
                .supervisor(userService.getById(projectForSave.getSupervisorId()))
                .chief(userService.getById(projectForSave.getChiefId()))
                .controller(userService.getById(projectForSave.getControllerId()))
                .company(companyService.getEntityById(projectForSave.getCompanyId()))
                .stage(stageService.getEntityById(projectForSave.getStageId()))
                .editTime(LocalDateTime.now())
                .reassemblyRequired(true)
                .build();
        ProjectEntity createdProject = projectRepository.createTransactional(projectForCreationEntity);
        documentService.createCoverPage(createdProject.getId());
        documentService.createTitlePage(createdProject.getId());
        documentService.createTableOfContents(createdProject.getId());
        reassemble(createdProject.getId());
        emailService.sendEmailOnProjectCreate(createdProject);
        return new ProjectDto(getById(createdProject.getId()));
    }

    public ProjectDto update(ProjectDto projectForUpdate) {
        ProjectEntity projectEntityForUpdate = getById(projectForUpdate.getId());
        projectEntityForUpdate.setName(Optional.ofNullable(projectForUpdate.getName())
                .orElse(projectEntityForUpdate.getName()));
        projectEntityForUpdate.setObjectName(Optional.ofNullable(projectForUpdate.getObjectName())
                .orElse(projectEntityForUpdate.getObjectName()));
        projectEntityForUpdate.setObjectAddress(Optional.ofNullable(projectForUpdate.getObjectAddress())
                .orElse(projectEntityForUpdate.getObjectAddress()));
        projectEntityForUpdate.setReleaseDate(Optional.ofNullable(projectForUpdate.getReleaseDate())
                .orElse(projectEntityForUpdate.getReleaseDate()));
        projectEntityForUpdate.setVolumeNumber(Optional.ofNullable(projectForUpdate.getVolumeNumber())
                .orElse(projectEntityForUpdate.getVolumeNumber()));
        projectEntityForUpdate.setVolumeName(Optional.ofNullable(projectForUpdate.getVolumeName())
                .orElse(projectEntityForUpdate.getVolumeName()));
        projectEntityForUpdate.setCode(Optional.ofNullable(projectForUpdate.getCode())
                .orElse(projectEntityForUpdate.getCode()));
        projectEntityForUpdate.setDesigner(Optional.ofNullable(projectForUpdate.getDesignerId())
                .map(userService::getById)
                .orElse(projectEntityForUpdate.getDesigner()));
        projectEntityForUpdate.setSupervisor(Optional.ofNullable(projectForUpdate.getSupervisorId())
                .map(userService::getById)
                .orElse(projectEntityForUpdate.getSupervisor()));
        projectEntityForUpdate.setChief(Optional.ofNullable(projectForUpdate.getChiefId())
                .map(userService::getById)
                .orElse(projectEntityForUpdate.getChief()));
        projectEntityForUpdate.setController(Optional.ofNullable(projectForUpdate.getControllerId())
                .map(userService::getById)
                .orElse(projectEntityForUpdate.getController()));
        projectEntityForUpdate.setStage(Optional.ofNullable(projectForUpdate.getStageId())
                .map(stageService::getEntityById)
                .orElse(projectEntityForUpdate.getStage()));
        projectEntityForUpdate.setReassemblyRequired(true);
        projectEntityForUpdate.setEditTime(LocalDateTime.now());
        ProjectEntity updatedProject = projectRepository
                .updateTransactional(projectEntityForUpdate);
        emailService.sendEmailOnProjectEdit(updatedProject);
        return new ProjectDto(updatedProject);
    }

    public void reassembleForController(Long projectId) {
        emailService.sendEmailOnProjectReassembly(projectRepository.fetchByIdWrapped(projectId));
        reassemble(projectId);
    }

    public void reassembleAll() {
        log.info("Try to reassemble by Scheduler all Projects with reassemblyRequired = true");
        Set<ProjectEntity> projectsForReassembly = projectRepository.fetchAllWithReassemblyRequired();
        projectsForReassembly.forEach(prj -> reassemble(prj.getId()));
        log.info("{} Projects reassembled by Scheduler", projectsForReassembly.size());
    }

    public void getFileForDownload(Long projectId, HttpServletResponse response) {
        ProjectEntity project = projectRepository.fetchByIdWrapped(projectId);
        byte[] documentInPdf = projectRepository.fetchProjectInPdfByProjectId(projectId);
        // TODO: 18.02.2022 store format in properties 
        String projectFileName = "%s_Том%d_%s.pdf".formatted(project.getCode(), project.getVolumeNumber(), project.getVolumeName());
        getFile(response, documentInPdf, projectFileName);
    }

    public DocumentDto setDesignerAndSupervisorInDocument(DocumentDto document) {
        ProjectDto project = getDtoById(document.getProjectId());
        if (document.getDesignerId() == null) {
            document.setDesignerId(project.getDesignerId());
        }
        if (document.getSupervisorId() == null) {
            document.setSupervisorId(project.getSupervisorId());
        }
        return document;
    }

    public void deleteById(Long projectId) {
        projectRepository.deleteProjectTransactional(projectId);
    }

    private ProjectEntity getEmpty() {
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

    private ProjectDto reassemble(Long projectId) {
        ProjectEntity project = projectRepository.fetchByIdWrapped(projectId);
        if (project.getReassemblyRequired()) {
            List<byte[]> documentsInBytes = new ArrayList<>();
            List<DocumentDto> documents = documentService.getAllByProjectId(projectId);
            documents.forEach(doc -> {
                documentService.reassembleDocument(doc.getId());
                documentsInBytes.add(documentService.getInPdfById(doc.getId()));
            });
            if (documentsInBytes.size() >= 1) {
                byte[] mergedDocument = documentsInBytes.get(0);
                for (int i = 1; i < documentsInBytes.size(); i++) {
                    mergedDocument = PdfDocumentGenerator.mergePdf(mergedDocument, documentsInBytes.get(i));
                }
                project = projectRepository.updateProjectInPdfTransactional(projectId, mergedDocument);
            } else project = projectRepository.updateProjectInPdfTransactional(projectId, null);
        }
        return new ProjectDto(project);
    }

    private List<ProjectDto> toDtoList(List<ProjectEntity> entityList) {
        return entityList.stream().
                filter(Objects::nonNull).
                map(ProjectDto::new).
                collect(Collectors.toList());
    }
}
