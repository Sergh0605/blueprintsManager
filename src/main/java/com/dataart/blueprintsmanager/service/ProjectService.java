package com.dataart.blueprintsmanager.service;

import com.dataart.blueprintsmanager.email.EmailService;
import com.dataart.blueprintsmanager.exceptions.InvalidInputDataException;
import com.dataart.blueprintsmanager.exceptions.NotFoundCustomApplicationException;
import com.dataart.blueprintsmanager.pdf.PdfDocumentGenerator;
import com.dataart.blueprintsmanager.persistence.entity.DocumentEntity;
import com.dataart.blueprintsmanager.persistence.entity.FileEntity;
import com.dataart.blueprintsmanager.persistence.entity.ProjectEntity;
import com.dataart.blueprintsmanager.persistence.repository.ProjectRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.dataart.blueprintsmanager.persistence.entity.DocumentType.*;

@Service
@Slf4j
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final DocumentService documentService;
    private final UserService userService;
    private final CompanyService companyService;
    private final StageService stageService;
    private final EmailService emailService;
    private final ProjectFileService projectFileService;


    public ProjectService(ProjectRepository projectRepository,
                          DocumentService documentService,
                          UserService userService,
                          CompanyService companyService,
                          StageService stageService,
                          EmailService emailService,
                          ProjectFileService projectFileService) {
        this.projectRepository = projectRepository;
        this.documentService = documentService;
        this.userService = userService;
        this.companyService = companyService;
        this.stageService = stageService;
        this.emailService = emailService;
        this.projectFileService = projectFileService;
    }

    @Transactional(readOnly = true)
    public Page<ProjectEntity> getAllNotDeletedPaginated(Pageable pageable) {
        return projectRepository.findAllByDeletedOrderByEditTimeDesc(false, pageable);
    }

    @Transactional(readOnly = true)
    public ProjectEntity getById(Long projectId) {
        return projectRepository.findById(projectId).orElseThrow(() -> {
            throw new NotFoundCustomApplicationException(String.format("Project with ID %d not found", projectId));
        });
    }

    @Transactional
    public ProjectEntity create(ProjectEntity projectForCreate) {
        if (isCodeDuplicated(projectForCreate.getCode())) {
            throw new InvalidInputDataException(String.format("Can't create project. Project with code = %s is already exists", projectForCreate.getCode()));
        }
        projectForCreate.setId(null);
        updateBasicFieldsWithExistenceCheck(projectForCreate);
        projectForCreate.setReassemblyRequired(true);
        projectForCreate.setDeleted(false);
        ProjectEntity createdProject = projectRepository.save(projectForCreate);
        documentService.createDefaultDocument(createdProject, COVER_PAGE);
        documentService.createDefaultDocument(createdProject, TITLE_PAGE);
        documentService.createDefaultDocument(createdProject, TABLE_OF_CONTENTS);
        ProjectEntity reassembledProject = reassemble(createdProject);
        emailService.sendEmailOnProjectCreate(reassembledProject);
        return reassembledProject;
    }

    @Transactional
    public DocumentEntity addNewEditableDocument(DocumentEntity document, MultipartFile file) {
        document.setProject(getById(document.getProject().getId()));
        document.setDocumentFile(new FileEntity());
        return documentService.createEditableDocumentForSave(document, file);
    }

    private void updateBasicFieldsWithExistenceCheck(ProjectEntity project) {
        project.setSupervisor(Optional.ofNullable(project.getSupervisor())
                .map(u -> userService.getByIdAndCompanyId(u.getId(), project.getCompany().getId()))
                .orElse(null));
        project.setDesigner(Optional.ofNullable(project.getDesigner())
                .map(u -> userService.getByIdAndCompanyId(u.getId(), project.getCompany().getId()))
                .orElse(null));
        project.setController(Optional.ofNullable(project.getController())
                .map(u -> userService.getByIdAndCompanyId(u.getId(), project.getCompany().getId()))
                .orElse(null));
        project.setChief(Optional.ofNullable(project.getChief())
                .map(u -> userService.getByIdAndCompanyId(u.getId(), project.getCompany().getId()))
                .orElse(null));
        project.setCompany(Optional.ofNullable(project.getCompany())
                .map(c -> companyService.getById(c.getId()))
                .orElse(null));
        project.setStage(Optional.ofNullable(project.getStage())
                .map(s -> stageService.getById(s.getId()))
                .orElse(null));
    }

    private Boolean isCodeDuplicated(String code) {
        return projectRepository.existsByCode(code);
    }

    @Transactional
    public ProjectEntity update(ProjectEntity projectForUpdate) {
        ProjectEntity currentProject = getById(projectForUpdate.getId());
        if (!currentProject.getCode().equals(projectForUpdate.getCode())) {
            if (isCodeDuplicated(projectForUpdate.getCode())) {
                throw new InvalidInputDataException(String.format("Can't edit project. Project with code = %s is already exists", projectForUpdate.getCode()));
            }
        }
        updateBasicFieldsWithExistenceCheck(projectForUpdate);
        currentProject.setName(projectForUpdate.getName());
        currentProject.setObjectName(projectForUpdate.getObjectName());
        currentProject.setObjectAddress(projectForUpdate.getObjectAddress());
        currentProject.setReleaseDate(projectForUpdate.getReleaseDate());
        currentProject.setVolumeName(projectForUpdate.getVolumeName());
        currentProject.setVolumeName(projectForUpdate.getVolumeName());
        currentProject.setCode(projectForUpdate.getCode());
        currentProject.setDesigner(projectForUpdate.getDesigner());
        currentProject.setSupervisor(projectForUpdate.getSupervisor());
        currentProject.setChief(projectForUpdate.getChief());
        currentProject.setController(projectForUpdate.getController());
        currentProject.setCompany(projectForUpdate.getCompany());
        currentProject.setStage(projectForUpdate.getStage());
        currentProject.setReassemblyRequired(true);
        ProjectEntity updatedProject = projectRepository.saveAndFlush(currentProject);
        emailService.sendEmailOnProjectEdit(updatedProject);
        return updatedProject;
    }

    @Transactional
    public ProjectEntity reassembleForController(Long projectId) {
        ProjectEntity projectForReassembly = getById(projectId);
        ProjectEntity reassembledProject = reassemble(projectForReassembly);
        emailService.sendEmailOnProjectReassembly(reassembledProject);
        return reassembledProject;
    }

    @Transactional
    public Integer reassembleAll() {
        Set<ProjectEntity> projectsForReassembly = projectRepository.findAllByReassemblyRequiredAndDeleted(true, false);
        projectsForReassembly.forEach(this::reassemble);
        return projectsForReassembly.size();
    }

    @Transactional
    public void setDeleted(Long projectId, Boolean deleted) {
        ProjectEntity projectForDelete = getById(projectId);
        projectForDelete.setDeleted(deleted);
        projectRepository.save(projectForDelete);
    }

    private ProjectEntity reassemble(ProjectEntity project) {
        if (project.getReassemblyRequired()) {
            List<byte[]> documentsInBytes = new ArrayList<>();
            List<DocumentEntity> documents = documentService.getAllByProjectId(project.getId());
            documents.forEach(doc -> {
                documentsInBytes.add(documentService.reassembleDocument(doc).getDocumentFile().getFileInBytes());
            });
            if (documentsInBytes.size() >= 1) {
                byte[] mergedDocument = documentsInBytes.get(0);
                for (int i = 1; i < documentsInBytes.size(); i++) {
                    mergedDocument = PdfDocumentGenerator.mergePdf(mergedDocument, documentsInBytes.get(i));
                }
                projectFileService.save(mergedDocument, project);
                project.setReassemblyRequired(false);
                return projectRepository.saveAndFlush(project);
            }
        }
        return project;
    }
}
