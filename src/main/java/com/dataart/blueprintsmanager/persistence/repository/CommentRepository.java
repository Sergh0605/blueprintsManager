package com.dataart.blueprintsmanager.persistence.repository;

import com.dataart.blueprintsmanager.exceptions.CustomApplicationException;
import com.dataart.blueprintsmanager.exceptions.DataBaseCustomApplicationException;
import com.dataart.blueprintsmanager.exceptions.NotFoundCustomApplicationException;
import com.dataart.blueprintsmanager.persistence.entity.CommentEntity;
import com.dataart.blueprintsmanager.persistence.entity.DocumentEntity;
import com.dataart.blueprintsmanager.persistence.entity.ProjectEntity;
import com.dataart.blueprintsmanager.persistence.entity.UserEntity;
import com.dataart.blueprintsmanager.util.CustomPage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
@AllArgsConstructor
public class CommentRepository {
    private final DataSource dataSource;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;

    public CustomPage<CommentEntity> fetchAllByProjectIdPaginated(Long projectId, Pageable pageable) {
        log.info("Try to find All Comments for Project with id = {}", projectId);
        int countForLimit = pageable.getPageSize();
        int countForOffset = (pageable.getPageNumber() - 1) * pageable.getPageSize();
        String getAllCommentsSql =
                "SELECT id, user_id as userId, content, publication_time as pubTime, project_id as projectId, document_id as documentId " +
                        "FROM bpm_comment " +
                        "WHERE deleted = 'false' AND project_id = ? " +
                        "ORDER BY pubTime DESC " +
                        "LIMIT ? " +
                        "OFFSET ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(getAllCommentsSql)) {
            pstmt.setLong(1, projectId);
            pstmt.setInt(2, countForLimit);
            pstmt.setInt(3, countForOffset);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                List<CommentEntity> commentEntityList = new ArrayList<>();
                while (resultSet.next()) {
                    CommentEntity comment = buildComment(resultSet);
                    commentEntityList.add(comment);
                }
                log.info(String.format("%d Comments found", commentEntityList.size()));
                Integer countOfComments = fetchCountOfCommentsByProjectId(projectId, connection);
                return new CustomPage<>(commentEntityList, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()), countOfComments);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataBaseCustomApplicationException("Database connection error.", e);
        }
    }

    public CustomPage<CommentEntity> fetchAllByDocumentIdPaginated(Long documentId, Pageable pageable) {
        log.info("Try to find All Comments for Document with id = {}", documentId);
        int countForLimit = pageable.getPageSize();
        int countForOffset = (pageable.getPageNumber() - 1) * pageable.getPageSize();
        String getAllCommentsSql =
                "SELECT id, user_id as userId, content, publication_time as pubTime, project_id as projectId, document_id as documentId " +
                        "FROM bpm_comment " +
                        "WHERE deleted = false AND document_id = ? " +
                        "ORDER BY pubTime DESC " +
                        "LIMIT ? " +
                        "OFFSET ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(getAllCommentsSql)) {
            pstmt.setLong(1, documentId);
            pstmt.setInt(2, countForLimit);
            pstmt.setInt(3, countForOffset);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                List<CommentEntity> commentEntityList = new ArrayList<>();
                while (resultSet.next()) {
                    CommentEntity comment = buildComment(resultSet);
                    commentEntityList.add(comment);
                }
                log.info(String.format("%d Companies found", commentEntityList.size()));
                Integer countOfComments = fetchCountOfCommentsByDocumentId(documentId, connection);
                return new CustomPage<>(commentEntityList, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()), countOfComments);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataBaseCustomApplicationException("Database connection error.", e);
        }
    }

    public CommentEntity createTransactional(CommentEntity commentForCreate) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                log.info(String.format("Try to transactional create new Comment for Project with id = %d", commentForCreate.getProject().getId()));
                Long createdCommentId = create(commentForCreate, connection);
                CommentEntity commentEntity = fetchById(createdCommentId, connection);
                connection.commit();
                log.info(String.format("Comment with id = %d is transactional created", commentEntity.getId()));
                return commentEntity;
            } catch (CustomApplicationException e) {
                connection.rollback();
                log.info(e.getMessage());
                throw e;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataBaseCustomApplicationException("Database connection error.", e);
        }
    }

    private Integer fetchCountOfCommentsByDocumentId(Long documentId, Connection connection) throws SQLException {
        log.info(String.format("Try to find Comments count for Project with with id = %d", documentId));
        String getCommentCountByProjectIdSql =
                "SELECT COUNT(*) as commentCount " +
                        "FROM bp_manager.public.bpm_comment " +
                        "WHERE deleted = 'false' AND  project_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(getCommentCountByProjectIdSql)) {
            pstmt.setLong(1, documentId);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    Integer countOfComments = resultSet.getInt("commentCount");
                    log.info(String.format("%d Comments found for Project id = %d ", countOfComments, documentId));
                    return countOfComments;
                }
            }
            return 0;
        }
    }

    private Integer fetchCountOfCommentsByProjectId(Long projectId, Connection connection) throws SQLException {
        log.info(String.format("Try to find Comments count for Project with with id = %d", projectId));
        String getCommentCountByProjectIdSql =
                "SELECT COUNT(*) as commentCount " +
                        "FROM bp_manager.public.bpm_comment " +
                        "WHERE deleted = false AND  project_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(getCommentCountByProjectIdSql)) {
            pstmt.setLong(1, projectId);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    Integer countOfComments = resultSet.getInt("commentCount");
                    log.info(String.format("%d Comments found for Document id = %d ", countOfComments, projectId));
                    return countOfComments;
                }
            }
            return 0;
        }
    }

    private CommentEntity buildComment(ResultSet resultSet) throws SQLException {
        return CommentEntity.builder()
                .id(resultSet.getLong("id"))
                .user(Optional.ofNullable(userRepository.fetchById(resultSet.getLong("userId"))).orElse(UserEntity.builder().login("USER NOT FOUND").build()))
                .text(Optional.ofNullable(resultSet.getString("content")).orElse(""))
                .publicationDateTime(resultSet.getTimestamp("pubTime").toLocalDateTime())
                .project(ProjectEntity.builder().id((Long) resultSet.getObject("projectId")).build())
                .document(DocumentEntity.builder().id((Long) resultSet.getObject("documentId")).build())
                .build();
    }

    private CommentEntity fetchById(Long commentId, Connection connection) throws SQLException {
        log.info(String.format("Try to find Comment with id = %d", commentId));
        String getCommnetByIdSql =
                "SELECT id, user_id as userId, project_id as projectId, document_id as documentId, content, " +
                        "publication_time as pubTime " +
                        "FROM bpm_comment " +
                        "WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(getCommnetByIdSql)) {
            pstmt.setLong(1, commentId);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    log.info(String.format("Comment with id = %d found", commentId));
                    return buildComment(resultSet);
                }
                throw new NotFoundCustomApplicationException(String.format("Comment with id= %d not found", commentId));
            }
        }
    }

    private Long create(CommentEntity comment, Connection connection) throws SQLException {
        log.info(String.format("Try to create new Comment for Project with id = %d", comment.getProject().getId()));
        String createCommentSql =
                "INSERT INTO bpm_comment ( " +
                        "user_id, project_id, document_id, content, publication_time) " +
                        "VALUES (?,?,?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(createCommentSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setLong(1, comment.getUser().getId());
            pstmt.setLong(2, comment.getProject().getId());
            pstmt.setObject(3, comment.getDocument().getId());
            pstmt.setString(4, comment.getText());
            pstmt.setTimestamp(5, Timestamp.valueOf(comment.getPublicationDateTime()));
            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long key = generatedKeys.getLong(1);
                    log.info(String.format("New Comment with id = %d created", key));
                    return key;
                } else {
                    throw new DataBaseCustomApplicationException("Creating comment failed, no ID obtained.");
                }
            }
        }
    }
}
