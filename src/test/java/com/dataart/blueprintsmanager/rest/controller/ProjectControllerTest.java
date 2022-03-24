package com.dataart.blueprintsmanager.rest.controller;

import com.dataart.blueprintsmanager.BlueprintsManagerApplication;
import com.dataart.blueprintsmanager.rest.dto.ExceptionDto;
import com.dataart.blueprintsmanager.rest.dto.ProjectDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = BlueprintsManagerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ProjectControllerTest extends BlueprintsManagerTest {

    @Test
    void givenValidProjectData_whenCreateByAdmin_then403() {
        //Given
        String adminAccessToken = getAdminTokens().getAccessToken();
        ProjectDto validProjectData = getValidProjectData();
        //When
        ResponseEntity<ProjectDto> response = createProject(validProjectData, adminAccessToken, ProjectDto.class);
        //Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(), "Wrong code");
    }

    @Test
    void givenValidProjectData_whenCreateByEditor_then201AndProjectReceived() {
        //Given
        long dbProjectsCountBefore = projectRepository.count();
        String editorAccessToken = getEditorTokens().getAccessToken();
        ProjectDto validProjectData = getValidProjectData();
        //When
        ResponseEntity<ProjectDto> response = createProject(validProjectData, editorAccessToken, ProjectDto.class);
        long dbProjectsCountAfter = projectRepository.count();
        //Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Wrong code");
        assertNotNull(response.getBody(), "Empty body");
        assertEquals(validProjectData.getName(), response.getBody().getName(), "Project name mismatch");
        assertEquals(dbProjectsCountBefore + 1, dbProjectsCountAfter, "DB Count of projects mismatch");
        assertEquals(3, documentRepository.findByProjectIdOrderByNumberInProject(response.getBody().getId()).size());
    }

    @Test
    void givenValidProjectData_whenCreateWithDuplicateCodeByEditor_then400AndExceptionReceived() {
        //Given
        String editorAccessToken = getEditorTokens().getAccessToken();
        ProjectDto validProjectData = getValidProjectData();
        createProject(validProjectData, editorAccessToken, ProjectDto.class);
        long dbProjectsCountBefore = projectRepository.count();
        //When
        ResponseEntity<ExceptionDto> response = createProject(validProjectData, editorAccessToken, ExceptionDto.class);
        long dbProjectsCountAfter = projectRepository.count();
        //Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Wrong code");
        assertNotNull(response.getBody(), "Empty body");
        assertEquals(400, response.getBody().getStatus(), "Body status mismatch");
        assertEquals(dbProjectsCountBefore, dbProjectsCountAfter, "DB Count of projects mismatch");
    }

    @Test
    void givenValidProjectDataForUpdate_whenUpdate_then201AndUpdatedProjectReceived() {
        //Given
        String editorAccessToken = getEditorTokens().getAccessToken();
        ProjectDto validProjectData = getValidProjectData();
        ResponseEntity<ProjectDto> resposeWithCreatedProject = createProject(validProjectData, editorAccessToken, ProjectDto.class);
        ProjectDto projectDataForUpdate = resposeWithCreatedProject.getBody();
        projectDataForUpdate.setObjectAddress("Новый адрес");
        //When
        ResponseEntity<ProjectDto> response = updateProject(projectDataForUpdate, editorAccessToken, ProjectDto.class);
        //Then
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Wrong code");
        assertNotNull(response.getBody(), "Empty body");
        assertEquals(projectDataForUpdate.getObjectAddress(), response.getBody().getObjectAddress(), "Address mismatch");
        assertTrue(projectRepository.findById(projectDataForUpdate.getId()).get().getReassemblyRequired(), "DB Count of projects mismatch");
    }

    @Test
    void givenInvalidValidProjectData_whenCreateByEditor_then400AndExceptionReceived() {
        //Given
        String editorAccessToken = getEditorTokens().getAccessToken();
        ProjectDto invalidProjectData = getInvalidProjectData();
        long dbProjectsCountBefore = projectRepository.count();
        //When
        ResponseEntity<ExceptionDto> response = createProject(invalidProjectData, editorAccessToken, ExceptionDto.class);
        long dbProjectsCountAfter = projectRepository.count();
        //Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Wrong code");
        assertNotNull(response.getBody(), "Empty body");
        assertEquals(400, response.getBody().getStatus(), "Body status mismatch");
        assertEquals(dbProjectsCountBefore, dbProjectsCountAfter, "DB Count of projects mismatch");
    }

    @Test
    void givenUpdatedProject_whenReassemble_then201AndReassembledProjectReceived() {
        //Given
        String editorAccessToken = getEditorTokens().getAccessToken();
        ProjectDto validProjectData = getValidProjectData();
        ProjectDto projectDataForUpdate = createProject(validProjectData, editorAccessToken, ProjectDto.class).getBody();
        projectDataForUpdate.setObjectAddress("Новый адрес");
        ProjectDto updatedProject = updateProject(projectDataForUpdate, editorAccessToken, ProjectDto.class).getBody();
        //When
        ResponseEntity<ProjectDto> response = reassembleProject(updatedProject.getId(), editorAccessToken, ProjectDto.class);
        //Then
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Wrong code");
        assertNotNull(response.getBody(), "Empty body");
        assertFalse(projectRepository.findById(projectDataForUpdate.getId()).get().getReassemblyRequired(), "DB Count of projects mismatch");
    }


}
