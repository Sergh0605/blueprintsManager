package com.dataart.blueprintsmanager.rest.controller;

import com.dataart.blueprintsmanager.rest.service.StageRestService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stage")
@Slf4j
@AllArgsConstructor
public class StageController {
private final StageRestService stageRestService;

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(stageRestService.getAllStages());
    }
}
