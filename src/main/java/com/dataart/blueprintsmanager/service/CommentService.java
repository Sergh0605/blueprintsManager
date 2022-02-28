package com.dataart.blueprintsmanager.service;

import com.dataart.blueprintsmanager.dto.CommentDto;
import com.dataart.blueprintsmanager.dto.DocumentDto;
import com.dataart.blueprintsmanager.exceptions.CustomApplicationException;
import com.dataart.blueprintsmanager.persistence.entity.CommentEntity;
import com.dataart.blueprintsmanager.persistence.entity.DocumentEntity;
import com.dataart.blueprintsmanager.persistence.entity.ProjectEntity;
import com.dataart.blueprintsmanager.persistence.entity.UserEntity;
import com.dataart.blueprintsmanager.persistence.repository.CommentRepository;
import com.dataart.blueprintsmanager.util.CustomPage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final ProjectService projectService;
    private final DocumentService documentService;
    private final UserService userService;

    public CustomPage<CommentDto> getPageOfCommentsForProject(Long projectId, Pageable pageable) {
        CustomPage<CommentEntity> pageOfCommentEntities = commentRepository.fetchAllByProjectIdPaginated(projectId, pageable);
        return new CustomPage<>(toDtoList(pageOfCommentEntities.getContent()), pageable, pageOfCommentEntities.getTotal());
    }

    public CustomPage<CommentDto> getPageOfCommentsForDocument(Long documentId, Pageable pageable) {
        CustomPage<CommentEntity> pageOfCommentEntities = commentRepository.fetchAllByDocumentIdPaginated(documentId, pageable);
        return new CustomPage<>(toDtoList(pageOfCommentEntities.getContent()), pageable, pageOfCommentEntities.getTotal());
    }

    private List<CommentDto> toDtoList(List<CommentEntity> entityList) {
        return entityList.stream().
                filter(Objects::nonNull).
                map(CommentDto::new).
                collect(Collectors.toList());
    }

    public CommentDto create(CommentDto comment) {
        if (comment == null || comment.getLogin() == null || (comment.getProjectId() == null && comment.getDocumentId() == null)) {
            throw new CustomApplicationException("Can't save comment. Wrong fields content.");
        } else {
            Long projectId = null;
            Long documentId = null;
            Long userId = userService.getByLogin(comment.getLogin()).getId();
            if (comment.getProjectId() != null) {
                projectId = projectService.getDtoById(comment.getProjectId()).getId();
            } else if (comment.getDocumentId() != null) {
                DocumentDto document = documentService.getById(comment.getDocumentId());
                documentId = document.getId();
                projectId = document.getProjectId();
            }
            CommentEntity commentForCreate = CommentEntity.builder()
                    .publicationDateTime(LocalDateTime.now())
                    .text(Optional.ofNullable(comment.getText()).orElse(""))
                    .project(ProjectEntity.builder().id(projectId).build())
                    .document(DocumentEntity.builder().id(documentId).build())
                    .user(UserEntity.builder().id(userId).build())
                    .build();
            return new CommentDto(commentRepository.createTransactional(commentForCreate));
        }
    }
}
