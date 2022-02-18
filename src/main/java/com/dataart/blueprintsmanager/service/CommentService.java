package com.dataart.blueprintsmanager.service;

import com.dataart.blueprintsmanager.dto.CommentDto;
import com.dataart.blueprintsmanager.persistence.entity.CommentEntity;
import com.dataart.blueprintsmanager.persistence.repository.CommentRepository;
import com.dataart.blueprintsmanager.util.CustomPage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;

    public CustomPage<CommentDto> getPageOfCommentsForProject(Long projectId, Pageable pageable) {
        CustomPage<CommentEntity> pageOfCommentEntities = commentRepository.fetchAllByProjectIdPaginated(projectId, pageable);
        CustomPage<CommentDto> pageOfDto = new CustomPage<>(toDtoList(pageOfCommentEntities.getContent()), pageable, pageOfCommentEntities.getTotal());
        return pageOfDto;
    }

    public CustomPage<CommentDto> getPageOfCommentsForDocument(Long documentId, Pageable pageable) {
        CustomPage<CommentEntity> pageOfCommentEntities = commentRepository.fetchAllByDocumentIdPaginated(documentId, pageable);
        return new CustomPage<>(toDtoList(pageOfCommentEntities.getContent()), pageable, pageOfCommentEntities.getTotalPages());
    }

    private List<CommentDto> toDtoList(List<CommentEntity> entityList) {
        return entityList.stream().
                filter(Objects::nonNull).
                map(CommentDto::new).
                collect(Collectors.toList());
    }
}
