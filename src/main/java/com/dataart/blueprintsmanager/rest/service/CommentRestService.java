package com.dataart.blueprintsmanager.rest.service;

import com.dataart.blueprintsmanager.aop.track.ParamName;
import com.dataart.blueprintsmanager.aop.track.UserAction;
import com.dataart.blueprintsmanager.aop.track.UserActivityTracker;
import com.dataart.blueprintsmanager.persistence.entity.CommentEntity;
import com.dataart.blueprintsmanager.rest.dto.BasicDto;
import com.dataart.blueprintsmanager.rest.dto.CommentDto;
import com.dataart.blueprintsmanager.rest.mapper.CommentMapper;
import com.dataart.blueprintsmanager.service.CommentService;
import com.dataart.blueprintsmanager.service.DocumentService;
import com.dataart.blueprintsmanager.service.ProjectService;
import com.dataart.blueprintsmanager.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class CommentRestService {
    private final CommentService commentService;
    private final CommentMapper commentMapper;
    private final ProjectService projectService;
    private final DocumentService documentService;
    private final UserService userService;

    public Page<CommentDto> getByProjectIdPaginated(Long projectId, Pageable pageable) {
        log.info("Try to get {} page with {} comments in Project with ID = {}", pageable.getPageNumber(), pageable.getPageSize(), projectId);
        Page<CommentEntity> commentEntityPage = commentService.getPageOfCommentsForProject(projectId, pageable);
        List<CommentDto> commentDtoList = commentEntityPage.getContent().stream().map(commentMapper::commentEntityToCommentDto).toList();
        Page<CommentDto> commentDtoPage = new PageImpl<CommentDto>(commentDtoList, pageable, commentEntityPage.getTotalElements());
        log.info("Page {} with {} Comments in Project with ID = {} found", commentDtoPage.getNumber(), commentDtoPage.getNumberOfElements(), projectId);
        return commentDtoPage;
    }

    public Page<CommentDto> getPageOfCommentsForDocument(Long projectId, Long documentId, Pageable pageable) {
        log.info("Try to get {} page with {} comments in Project with ID = {} for Document with ID = {}", pageable.getPageNumber(), pageable.getPageSize(), projectId, documentId);
        Page<CommentEntity> commentEntityPage = commentService.getPageOfCommentsForDocument(projectId, documentId, pageable);
        List<CommentDto> commentDtoList = commentEntityPage.getContent().stream().map(commentMapper::commentEntityToCommentDto).toList();
        Page<CommentDto> commentDtoPage = new PageImpl<CommentDto>(commentDtoList, pageable, commentEntityPage.getTotalElements());
        log.info("Page {} with {} Comments in Project with ID = {} for Document with ID = {} found", commentDtoPage.getNumber(), commentDtoPage.getNumberOfElements(), projectId, documentId);
        return commentDtoPage;
    }

    @UserActivityTracker(action = UserAction.CREATE_COMMENT, projectId = "#projectId.toString()")
    public CommentDto createNewCommentForProject(@ParamName("projectId") Long projectId, CommentDto commentDto) {
        log.info("Try to create new Comment for Project with ID = {}", projectId);
        commentDto.setProject(new BasicDto(projectId));
        CommentEntity createdComment = commentService.createNewComment(commentMapper.commentDtoToCommentEntity(commentDto));
        CommentDto createdCommentDto = commentMapper.commentEntityToCommentDto(createdComment);
        log.info("Comment for Project with ID = {} created with ID = {}", createdCommentDto.getProject().getId(), createdCommentDto.getId());
        return createdCommentDto;
    }

    @UserActivityTracker(action = UserAction.CREATE_COMMENT, projectId = "#projectId.toString()")
    public CommentDto createNewCommentForDocument(@ParamName("projectId") Long projectId, Long documentId, CommentDto commentDto) {
        log.info("Try to create new Comment for Document with ID = {} in Project with ID = {}", documentId, projectId);
        commentDto.setProject(new BasicDto(projectId));
        commentDto.setDocument(new BasicDto(documentId));
        CommentEntity createdComment = commentService.createNewComment(commentMapper.commentDtoToCommentEntity(commentDto));
        CommentDto createdCommentDto = commentMapper.commentEntityToCommentDto(createdComment);
        log.info("Comment for Project with ID = {} created with ID = {}", createdCommentDto.getProject().getId(), createdCommentDto.getId());
        return createdCommentDto;
    }

    @UserActivityTracker(action = UserAction.DELETE_COMMENT, commentId = "#commentId.toString()")
    public void deleteById(Long projectId, @ParamName("commentId") Long commentId) {
        log.info("Try to mark as deleted Comment with ID = {} in Project with ID = {}", commentId, projectId);
        commentService.setDeletedByIdAndProjectId(commentId ,projectId, true);
        log.info("Comment with ID = {} marked as deleted.", projectId);
    }

    @UserActivityTracker(action = UserAction.RESTORE_COMMENT, commentId = "#commentId.toString()")
    public void restoreById(Long projectId, @ParamName("commentId") Long commentId) {
        log.info("Try to restore Comment with ID = {} in Project with ID = {}", projectId, projectId);
        commentService.setDeletedByIdAndProjectId(commentId ,projectId, false);
        log.info("Comment with ID = {} restored.", projectId);
    }
}
