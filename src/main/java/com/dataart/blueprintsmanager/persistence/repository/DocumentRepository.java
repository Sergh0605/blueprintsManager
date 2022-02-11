package com.dataart.blueprintsmanager.persistence.repository;

import com.dataart.blueprintsmanager.exceptions.DataBaseCustomApplicationException;
import com.dataart.blueprintsmanager.persistence.entity.DocumentEntity;
import com.dataart.blueprintsmanager.persistence.entity.DocumentType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
@AllArgsConstructor
public class DocumentRepository {
    private final DataSource dataSource;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    private DocumentEntity buildDocument(ResultSet resultSet, Connection connection) {
        try {
            return DocumentEntity.builder()
                    .id(resultSet.getLong("id"))
                    .project(projectRepository.fetchById(resultSet.getLong("projectId"), connection))
                    .numberInProject(resultSet.getInt("number"))
                    .documentType(DocumentType.getById(resultSet.getLong("typeId")))
                    .name(resultSet.getString("name"))
                    .code(resultSet.getString("code"))
                    .designer(userRepository.fetchById(resultSet.getLong("designerId"), connection))
                    .supervisor(userRepository.fetchById(resultSet.getLong("supervisorId"), connection))
                    .reassemblyRequired(resultSet.getBoolean("reassembly"))
                    .editTime(resultSet.getTimestamp("editTime").toLocalDateTime())
                    .build();
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Can't parse Document object from DB");
        }
    }

    public List<DocumentEntity> fetchAllByProjectIdTransactional(Long projectId) {
        String getByProjectIdSql =
                "SELECT id, project_id as projectId, number_in_project as number, type_id as typeId, name, code, " +
                        "designer_id as designerId, supervisor_id as supervisorId, reassembly_required as reassembly, edit_time as editTime " +
                        "FROM bpm_document " +
                        "WHERE deleted = 'false' AND project_id = ? " +
                        "ORDER BY number_in_project";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(getByProjectIdSql)
        ) {
            pstmt.setLong(1, projectId);
            log.debug(String.format("Try to find Documents for Project with id = %d", projectId));
            try (ResultSet resultSet = pstmt.executeQuery()) {
                Integer documentsCount = 0;
                List<DocumentEntity> documentEntityList = new ArrayList<>();
                while (resultSet.next()) {
                    DocumentEntity document = buildDocument(resultSet, connection);
                    documentEntityList.add(document);
                    documentsCount++;
                }
                log.debug(String.format("%d Documents found for Project with id = %d", documentsCount, projectId));
                return documentEntityList;
            }
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Database connection error.");
        }
    }

    public DocumentEntity fetchByIdTransactional(Long documentId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            DocumentEntity documentEntity = fetchById(documentId, connection);
            connection.commit();
            log.debug(String.format("Document with id = %d found", documentId));
            return documentEntity;
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Database connection error.");
        }
    }

    private DocumentEntity fetchById(Long documentId, Connection connection) throws SQLException {
        log.debug(String.format("Try to find Document with id = %d", documentId));
        String getDocumentByIdSql =
                "SELECT id, project_id as projectId, number_in_project as number, type_id as typeId, name, code, " +
                        "designer_id as designerId, supervisor_id as supervisorId, reassembly_required as reassembly, edit_time as editTime " +
                        "FROM bpm_document " +
                        "WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(getDocumentByIdSql)) {
            pstmt.setLong(1, documentId);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    log.debug(String.format("Document with id = %d found", documentId));
                    return buildDocument(resultSet, connection);
                }
                String message = String.format("Project with id= %d not found", documentId);
                log.debug(message);
                throw new DataBaseCustomApplicationException(message);
            }
        }
    }
}
