package com.dataart.blueprintsmanager.rest.controller;

import com.dataart.blueprintsmanager.rest.dto.ProjectDto;
import com.dataart.blueprintsmanager.rest.service.ProjectRestService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;


@RestController
@RequestMapping("/api/project")
@Slf4j
@AllArgsConstructor
public class ProjectController {
    private final ProjectRestService projectRestService;

    @GetMapping
    public ResponseEntity<?> indexPageable(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "5") Integer size,
            @RequestParam(name = "search", defaultValue = "") String search) {
        return ResponseEntity.ok(projectRestService.getAllNotDeletedPaginated(PageRequest.of(page, size), search));
    }

    @GetMapping("/{projectId}/assemble_history")
    public ResponseEntity<?> assembleHistoryPageable(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "5") Integer size,
            @PathVariable Long projectId) {
        return ResponseEntity.ok(projectRestService.getAssembleHistoryPaginated(projectId, PageRequest.of(page, size, Sort.by("creationTime").descending())));
    }

    @GetMapping("/{projectId}/assemble_history/{projectInPdfId}/download")
    public ResponseEntity<?> getPdfFromHistory(
            @PathVariable Long projectId,
            @PathVariable Long projectInPdfId,
            HttpServletResponse response) {
        projectRestService.getPdfFromHistoryForDownload(projectId, projectInPdfId, response);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = {"/{projectId}"})
    public ResponseEntity<?> viewProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectRestService.getById(projectId));
    }

    @PutMapping(value = {"/{projectId}"})
    public ResponseEntity<?> editProject(@PathVariable Long projectId,
                                         @RequestBody @Valid ProjectDto project) {
        return ResponseEntity.ok(projectRestService.update(projectId, project));
    }

    @PostMapping(value = {"/{projectId}/disable"})
    public ResponseEntity<?> deleteProject(@PathVariable Long projectId) {
        projectRestService.deleteById(projectId);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = {"/{projectId}/enable"})
    public ResponseEntity<?> restoreProject(@PathVariable Long projectId) {
        projectRestService.restoreById(projectId);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<?> saveProject(@RequestBody @Valid ProjectDto project) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectRestService.create(project));
    }

    @GetMapping(value = {"/{projectId}/download"})
    public ResponseEntity<?> serveFile(@PathVariable Long projectId, HttpServletResponse response) {
        projectRestService.getFileForDownload(projectId, response);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = {"/{projectId}/assemble"})
    public ResponseEntity<?> reassemble(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectRestService.assemble(projectId));
    }
}
