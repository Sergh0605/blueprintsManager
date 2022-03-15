package com.dataart.blueprintsmanager.rest.controller;

import com.dataart.blueprintsmanager.rest.dto.CommentDto;
import com.dataart.blueprintsmanager.rest.service.CommentRestService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/project/{projectId}/")
@Slf4j
@AllArgsConstructor
public class CommentController {
    private final CommentRestService commentRestService;

    @GetMapping(value = "/comment")
    public ResponseEntity<?> projectCommentsPage(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "5") Integer size,
            @PathVariable Long projectId) {
        return ResponseEntity.ok(commentRestService.getByProjectIdPaginated(projectId, PageRequest.of(page, size)));
    }

    @GetMapping(value = "/document/{documentId}/comment")
    public ResponseEntity<?> documentCommentsPage(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "5") Integer size,
            @PathVariable Long projectId,
            @PathVariable Long documentId) {
        return ResponseEntity.ok(commentRestService.getPageOfCommentsForDocument(projectId, documentId, PageRequest.of(page, size)));
    }

    @PostMapping("/comment")
    public ResponseEntity<?> newCommentForProject(@RequestBody @Valid CommentDto comment,
                                                  @PathVariable Long projectId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentRestService.createNewCommentForProject(projectId, comment));
    }

    @PostMapping("/document/{documentId}/comment")
    public ResponseEntity<?> newCommentForDocument(@RequestBody @Valid CommentDto comment,
                                                   @PathVariable Long projectId,
                                                   @PathVariable Long documentId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentRestService.createNewCommentForDocument(projectId, documentId, comment));
    }

    @PostMapping(value = "/comment/{commentId}/disable")
    public ResponseEntity<?> deleteById(@PathVariable Long projectId,
                                        @PathVariable Long commentId) {
        commentRestService.deleteById(projectId, commentId);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/comment/{commentId}/enable")
    public ResponseEntity<?> restoreById(@PathVariable Long projectId,
                                         @PathVariable Long commentId) {
        commentRestService.restoreById(projectId, commentId);
        return ResponseEntity.ok().build();
    }
}
