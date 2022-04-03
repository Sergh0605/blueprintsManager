package com.dataart.blueprintsmanager.rest.controller;

import com.dataart.blueprintsmanager.rest.service.DocTypeRestService;
import com.dataart.blueprintsmanager.rest.service.RoleRestService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/project/document-type")
@Slf4j
@AllArgsConstructor
public class DocTypeController {
    private final DocTypeRestService docTypeRestService;

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(docTypeRestService.getAllDocTypes());
    }
}
