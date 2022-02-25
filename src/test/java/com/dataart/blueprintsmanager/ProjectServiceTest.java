package com.dataart.blueprintsmanager;

import com.dataart.blueprintsmanager.dto.DocumentDto;
import com.dataart.blueprintsmanager.dto.ProjectDto;
import com.dataart.blueprintsmanager.exceptions.EditProjectException;
import com.dataart.blueprintsmanager.persistence.entity.DocumentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.util.List;


class ProjectServiceTest extends BlueprintsManagerApplicationTests {


    @Test
    void getDtoById() {
        Long id = 1L;
        String code = "TestCode";
        ProjectDto createdProjectDto = projectService.create(getDto(id, code));
        ProjectDto fetchedProjectDto = projectService.getDtoById(createdProjectDto.getId());
        Assert.isTrue(createdProjectDto.equals(fetchedProjectDto), "Wrong Project object");
    }

    @Test
    void create() {
        Long id = null;
        String code = "TestCode";
        ProjectDto createdProjectDto = projectService.create(getDto(id, code));
        List<DocumentDto> documentDtos = documentService.getAllByProjectId(createdProjectDto.getId());
        byte[] projectInPdf = projectRepository.fetchProjectInPdfByProjectId(createdProjectDto.getId());
        Assert.isTrue(!createdProjectDto.getReassemblyRequired(), "Project is not assembled.");
        Assert.isTrue(documentDtos.size() == 3, "Less than 3 Documents created in Project");
        Assert.isTrue(projectInPdf != null && projectInPdf.length > 0,
                "Project in PDF is Empty after creation and assembly.");
        Assert.isTrue(documentDtos.stream().allMatch(d ->
                        (d.getNumberInProject() == 1 && DocumentType.COVER_PAGE.equals(d.getType())) ||
                                (d.getNumberInProject() == 2 && DocumentType.TITLE_PAGE.equals(d.getType())) ||
                                (d.getNumberInProject() == 3 && DocumentType.TABLE_OF_CONTENTS.equals(d.getType()))),
                "Incorrect type of created documents in Project.");
    }

    @Test
    void createWithDuplicateCode() {
        Long id = null;
        String code = "TestCode";
        projectService.create(getDto(id, code));
        Assertions.assertThrows(EditProjectException.class, () -> {
            projectService.create(getDto(id, code));
        }, "Project with duplicated Code created.");
    }

    @Test
    void update() {
        Long id = null;
        String code = "TestCode";
        String newProjectName = "Обновленный тестовый том.";
        ProjectDto createdProject = projectService.create(getDto(id, code));
        createdProject.setName(newProjectName);
        ProjectDto updatedProject = projectService.update(createdProject);
        Assert.isTrue(newProjectName.equals(updatedProject.getName()) && updatedProject.getReassemblyRequired(),
                "Project not updated.");
    }

    @Test
    void updateWithDuplicateCode() {
        Long id = null;
        String codeForExistedProject = "Old Code";
        String codeForUpdatableProject = "Updatable Code";
        projectService.create(getDto(id, codeForExistedProject));
        ProjectDto createdProject = projectService.create(getDto(id, codeForUpdatableProject));
        createdProject.setCode(codeForExistedProject);
        Assertions.assertThrows(EditProjectException.class, () -> {
            projectService.update(createdProject);
        }, "Project with duplicated Code updated.");
    }

    @Test
    void reassembleForController() {
        Long id = null;
        String code = "TestCode";
        String newProjectName = "Обновленный тестовый том.";
        ProjectDto createdProject = projectService.create(getDto(id, code));
        createdProject.setName(newProjectName);
        ProjectDto updatedProject = projectService.update(createdProject);
        byte[] projectInPdfAfterUpdate = projectRepository.fetchProjectInPdfByProjectId(updatedProject.getId());
        projectService.reassembleForController(updatedProject.getId());
        byte[] projectInPdfAfterReassembly = projectRepository.fetchProjectInPdfByProjectId(updatedProject.getId());
        Assertions.assertNotEquals(projectInPdfAfterUpdate, projectInPdfAfterReassembly, "Project was not reassembled after update.");
    }

    private ProjectDto getDto(Long id, String code) {
        return ProjectDto.builder()
                .id(id)
                .code(code)
                .name("Тестовый проект")
                .objectName("Тестовый объект")
                .objectAddress("Тестовый адрес")
                .releaseDate(LocalDate.now())
                .volumeNumber(1L)
                .volumeName("Тестовый том")
                .designerId(1L)
                .supervisorId(2L)
                .chiefId(3L)
                .controllerId(4L)
                .companyId(1L)
                .stageId(1L)
                .build();
    }


}