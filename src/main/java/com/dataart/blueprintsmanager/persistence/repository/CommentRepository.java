package com.dataart.blueprintsmanager.persistence.repository;

import com.dataart.blueprintsmanager.exceptions.DataBaseCustomApplicationException;
import com.dataart.blueprintsmanager.persistence.entity.CommentEntity;
import com.dataart.blueprintsmanager.persistence.entity.UserEntity;
import com.dataart.blueprintsmanager.util.CustomPage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
                "SELECT id, user_id as userId, content, publication_time as pubTime " +
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

    protected Integer fetchCountOfCommentsByProjectId(Long projectId, Connection connection) throws SQLException {
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

    public CustomPage<CommentEntity> fetchAllByDocumentIdPaginated(Long documentId, Pageable pageable) {
        log.info("Try to find All Comments for Document with id = {}", documentId);
        String getAllCommentsSql =
                "SELECT id, user_id as userId, content, publication_time as pubTime " +
                        "FROM bpm_comment " +
                        "WHERE deleted = 'false' AND document_id = ? " +
                        "ORDER BY pubTime DESC " +
                        "LIMIT ? " +
                        "OFFSET ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(getAllCommentsSql)) {
            pstmt.setLong(1, documentId);
            pstmt.setInt(2, pageable.getPageNumber() * pageable.getPageSize());
            pstmt.setInt(3, (pageable.getPageNumber() - 1) * pageable.getPageSize());
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

    protected Integer fetchCountOfCommentsByDocumentId(Long documentId, Connection connection) throws SQLException {
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

    private CommentEntity buildComment(ResultSet resultSet) throws SQLException {
        return CommentEntity.builder()
                .id(resultSet.getLong("id"))
                .user(Optional.ofNullable(userRepository.fetchById(resultSet.getLong("userId"))).orElse(UserEntity.builder().login("LOGIN").build()))
                .text(Optional.ofNullable(resultSet.getString("content")).orElse(""))
                .publicationDateTime(resultSet.getTimestamp("pubTime").toLocalDateTime())
                .build();
    }
}
