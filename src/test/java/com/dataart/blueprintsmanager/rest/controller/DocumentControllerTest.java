package com.dataart.blueprintsmanager.rest.controller;

import com.dataart.blueprintsmanager.BlueprintsManagerApplication;
import com.dataart.blueprintsmanager.persistence.entity.DocumentEntity;
import com.dataart.blueprintsmanager.rest.dto.ProjectDto;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = BlueprintsManagerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class DocumentControllerTest extends BlueprintsManagerTest {

    @Test
    void givenValidDocData_whenCreateDocInThreads_thenDocsWithUniqNumsInPrj() throws InterruptedException {
        //Given
        int threadCount = 10;
        int addDocAttemptsPerThread = 10;
        int expectedCountOfDocs = threadCount * addDocAttemptsPerThread + 3;
        int expectedCountOfActivities = threadCount * addDocAttemptsPerThread * 2;
        String editorAccessToken = getEditorTokens().getAccessToken();
        ProjectDto validProjectData = getValidProjectData();
        ResponseEntity<ProjectDto> response1 = createProject(validProjectData, editorAccessToken, ProjectDto.class);
        Long projectId1 = response1.getBody().getId();
        validProjectData.setCode("randomCode");
        ResponseEntity<ProjectDto> response2 = createProject(validProjectData, editorAccessToken, ProjectDto.class);
        Long projectId2 = response2.getBody().getId();
        CountDownLatch latch = new CountDownLatch(1);
        Set<CreateDocWithCountdownLatch> setOfThreads = new HashSet<>();
        int x = 0;
        while (x < threadCount) {
            setOfThreads.add(new CreateDocWithCountdownLatch(editorAccessToken ,latch, projectId1, addDocAttemptsPerThread));
            setOfThreads.add(new CreateDocWithCountdownLatch(editorAccessToken ,latch, projectId2, addDocAttemptsPerThread));
            x++;
        }
        long dbUserActivitiesCountBefore = userActivityRepository.count();
        //When
        setOfThreads.forEach(CreateDocWithCountdownLatch::start);
        latch.countDown();
        Thread.sleep(10000);
        //Then
        long dbUserActivitiesCountAfter = userActivityRepository.count();
        List<DocumentEntity> docs1 = documentRepository.findByProjectIdOrderByNumberInProject(projectId1);
        List<DocumentEntity> docs2 = documentRepository.findByProjectIdOrderByNumberInProject(projectId2);
        long countOfDocsWithUniqNumsInPrj1 = docs1.stream().map(DocumentEntity::getNumberInProject).collect(Collectors.toSet()).size();
        long countOfDocsWithUniqNumsInPrj2 = docs2.stream().map(DocumentEntity::getNumberInProject).collect(Collectors.toSet()).size();
        System.out.println("Activity count: " + (dbUserActivitiesCountAfter - dbUserActivitiesCountBefore));
        System.out.println("Count of added Documents in prj1: " + (docs1.size() - 3));
        System.out.println("Count of added Documents in prj2: " + (docs2.size() - 3));
        assertEquals(dbUserActivitiesCountAfter - dbUserActivitiesCountBefore, expectedCountOfActivities);
        assertEquals(countOfDocsWithUniqNumsInPrj1, docs1.size());
        assertEquals(countOfDocsWithUniqNumsInPrj2, docs2.size());
    }
}
