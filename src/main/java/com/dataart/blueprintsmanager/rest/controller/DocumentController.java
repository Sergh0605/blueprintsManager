package com.dataart.blueprintsmanager.rest.controller;

import com.dataart.blueprintsmanager.rest.dto.DocumentDto;
import com.dataart.blueprintsmanager.rest.service.DocumentRestService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/project/{projectId}/document")
@Slf4j
@AllArgsConstructor
public class DocumentController {
    private final DocumentRestService documentRestService;

    @GetMapping(value = {"/{documentId}/download"})
    public ResponseEntity<?> serveFile(@PathVariable Long projectId, @PathVariable Long documentId, HttpServletResponse response) {
        documentRestService.getFileForDownload(projectId, documentId, response);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = {"/{documentId}/disable"})
    public ResponseEntity<?> deleteDocument(@PathVariable Long projectId, @PathVariable Long documentId) {
        documentRestService.deleteById(projectId, documentId);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = {"/{documentId}/enable"})
    public ResponseEntity<?> restoreDocument(@PathVariable Long projectId, @PathVariable Long documentId) {
        documentRestService.restoreById(projectId, documentId);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = {"/{documentId}/assemble"})
    public ResponseEntity<?> reassemble(@PathVariable Long projectId, @PathVariable Long documentId) {
        return ResponseEntity.ok(documentRestService.assemble(projectId, documentId));
    }

    @GetMapping(value = {"/{documentId}"})
    public ResponseEntity<?> viewDocument(@PathVariable Long projectId, @PathVariable Long documentId) {
        return ResponseEntity.ok(documentRestService.getByIdAndProjectId(projectId, documentId));
    }

    @PutMapping(value = {"/{documentId}"})
    public ResponseEntity<?> editDocument(@PathVariable Long projectId,
                                          @PathVariable Long documentId,
                                          @RequestPart @Valid DocumentDto document,
                                          @RequestPart(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.ok(documentRestService.update(projectId, documentId, document, file));
    }

    @GetMapping
    public ResponseEntity<?> getDocumentsByProjectId(@PathVariable Long projectId) {
        return ResponseEntity.ok(documentRestService.getByProjectId(projectId));
    }

    @PostMapping
    public ResponseEntity<?> createNewDocument(@RequestPart("document") @Valid DocumentDto document,
                                               @RequestPart(value = "file", required = false) MultipartFile file,
                                               @PathVariable Long projectId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentRestService.create(projectId, document, file));
    }
}
