package com.dataart.blueprintsmanager.rest.controller;

import com.dataart.blueprintsmanager.rest.dto.DocumentDto;
import com.dataart.blueprintsmanager.rest.dto.DocumentTypeDto;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.concurrent.CountDownLatch;

public class CreateDocWithCountdownLatch extends Thread {
    private CountDownLatch latch;
    private String token;
    private long projectId;

    private int attemptsCount;

    public CreateDocWithCountdownLatch(String token, CountDownLatch latch, Long projectId, int attemptsCount) {
        this.latch =latch;
        this.projectId = projectId;
        this.token = token;
        this.attemptsCount = attemptsCount;
    }

    @Override
    public void run() {
        try {
            latch.await();
            int x = 0;
            while (x < attemptsCount) {
                createNewDocument();
                x++;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String createURLWithPort(String uri) {
        return "http://localhost:" + "8085" + uri;
    }

    protected void createNewDocument() {
        DocumentDto documentDto = DocumentDto.builder()
                .documentType(DocumentTypeDto.builder().id(4L).build())
                .name("Тестовый документ")
                .code("ПЗ")
                .version(1L)
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("document", documentDto);
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(map, headers);
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        ResponseEntity<String> response = testRestTemplate.exchange(
                createURLWithPort("/api/project/" + projectId + "/document"),
                HttpMethod.POST, entity, String.class);
    }
}
