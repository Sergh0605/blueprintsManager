package com.dataart.blueprintsmanager.service;

import com.dataart.blueprintsmanager.exceptions.NotFoundCustomApplicationException;
import com.dataart.blueprintsmanager.persistence.entity.CommentEntity;
import com.dataart.blueprintsmanager.persistence.repository.CommentRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final ProjectService projectService;
    private final DocumentService documentService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public Page<CommentEntity> getPageOfCommentsForProject(Long projectId, Pageable pageable) {
        projectService.getById(projectId);
        return commentRepository.findByProjectIdAndDeletedOrderByPublicationDateTimeDesc(projectId, false, pageable);
    }

    @Transactional(readOnly = true)
    public Page<CommentEntity> getPageOfCommentsForDocument(Long projectId, Long documentId, Pageable pageable) {
        projectService.getById(projectId);
        return commentRepository.findByDocumentIdAndProjectIdAndDeletedOrderByPublicationDateTimeDesc(documentId, projectId, false, pageable);
    }

    @Transactional
    public CommentEntity createNewComment(CommentEntity comment) {
        CommentEntity commentForCreate = CommentEntity.builder()
                .id(null)
                .text(Optional.ofNullable(comment.getText()).orElse(""))
                .project(projectService.getById(comment.getProject().getId()))
                .document(Optional.ofNullable(comment.getDocument()).map(d -> documentService.getByIdAndProjectId(d.getId(), comment.getProject().getId())).orElse(null))
                .user(userService.getById(comment.getUser().getId()))
                .deleted(false)
                .build();
        return commentRepository.saveAndFlush(commentForCreate);
    }

    @Transactional
    public void setDeletedByIdAndProjectId(Long commentId, Long projectId, Boolean deleteStatus) {
        CommentEntity commentForDelete = getByIdAndProjectId(commentId, projectId);
        commentForDelete.setDeleted(deleteStatus);
        commentRepository.save(commentForDelete);
    }

    private CommentEntity getByIdAndProjectId(Long commentId, Long projectId) {
        return commentRepository.findByIdAndProjectId(commentId, projectId).orElseThrow(() -> {
            throw new NotFoundCustomApplicationException(String.format("Comment with ID = %d not found in Project with ID = %d", commentId, projectId));
        });
    }

}
