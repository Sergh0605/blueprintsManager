package com.dataart.blueprintsmanager.persistence.repository;

import com.dataart.blueprintsmanager.exceptions.DataBaseCustomApplicationException;
import com.dataart.blueprintsmanager.persistence.entity.DocumentEntity;
import com.dataart.blueprintsmanager.persistence.entity.DocumentType;
import com.dataart.blueprintsmanager.persistence.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
                    .designer(getUser((Long)resultSet.getObject("designerId"), connection))
                    .supervisor(getUser((Long)resultSet.getObject("supervisorId"), connection))
                    .reassemblyRequired(resultSet.getBoolean("reassembly"))
                    .editTime(resultSet.getTimestamp("editTime").toLocalDateTime())
                    .build();
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Can't parse Document object from DB");
        }
    }

    private UserEntity getUser(Long userId, Connection connection){
        return Optional.ofNullable(userId).map(x -> userRepository.fetchById(x, connection)).orElse(null);
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

    public DocumentEntity createTransactional(DocumentEntity document) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                log.debug(String.format("Try to create new %s Document for Project with id = %d", document.getDocumentType().name(), document.getProject().getId()));
                Long createdDocumentId = create(document, connection);
                DocumentEntity documentEntity = fetchById(createdDocumentId, connection);
                connection.commit();
                log.debug(String.format("Document with id = %d is created", documentEntity.getId()));
                return documentEntity;
            } catch (SQLException e) {
                connection.rollback();
                log.debug(e.getMessage());
                throw new DataBaseCustomApplicationException("Database unexpected error.");
            }
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Database connection error.");
        }
    }

    private Long create(DocumentEntity document, Connection connection) throws SQLException {
        String createDocumentSql =
                "INSERT INTO bpm_document ( " +
                        "project_id, number_in_project, type_id, name, code, designer_id, supervisor_id, content_in_pdf,reassembly_required, " +
                        "edit_time) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(createDocumentSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setLong(1, document.getProject().getId());
            pstmt.setInt(2, document.getNumberInProject());
            pstmt.setLong(3, document.getDocumentType().getId());
            pstmt.setString(4, document.getName());
            pstmt.setString(5, document.getCode());
            pstmt.setObject(6, document.getDesigner().getId());
            pstmt.setObject(7, document.getSupervisor().getId());
            pstmt.setBytes(8, document.getContentInPdf());
            pstmt.setBoolean(9, document.getReassemblyRequired());
            pstmt.setTimestamp(10, Timestamp.valueOf(document.getEditTime()));
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating document failed, no rows affected.");
            }
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else {
                    throw new SQLException("Creating project failed, no ID obtained.");
                }
            }
        }
    }
    public void updateContentInPdfTransactional(Long documentId, byte[] documentInPdf) {
        String updateContentInDocumentByIdSql =
                "UPDATE  bpm_document SET content_in_pdf = ? " +
                        "WHERE id = ?";
        try (Connection connection = dataSource.getConnection()) {
            try {
                log.debug(String.format("Try to update document content in PDF with id = %d", documentId));
                try (PreparedStatement pstmt = connection.prepareStatement(updateContentInDocumentByIdSql)) {
                    pstmt.setBytes(1, documentInPdf);
                    pstmt.setLong(2, documentId);
                    pstmt.executeUpdate();
                    log.debug(String.format("Content in PDF updated in Document with id = %d", documentId));
                }
            } catch (SQLException e) {
                connection.rollback();
                log.debug(e.getMessage());
                throw new DataBaseCustomApplicationException("Database unexpected error.");
            }
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Database connection error.");
        }
    }

    public void updateDocumentInPdfTransactional(Long documentId, byte[] documentInPdf) {
        String updateFileInDocumentByIdSql =
                "UPDATE  bpm_document SET document_in_pdf = ?, reassembly_required = false " +
                        "WHERE id = ?";
        try (Connection connection = dataSource.getConnection()) {
            try {
                log.debug(String.format("Try to update Document file in PDF with id = %d", documentId));
                try (PreparedStatement pstmt = connection.prepareStatement(updateFileInDocumentByIdSql)) {
                    pstmt.setBytes(1, documentInPdf);
                    pstmt.setLong(2, documentId);
                    pstmt.executeUpdate();
                    log.debug(String.format("Document in PDF updated in Document with id = %d", documentId));
                }
            } catch (SQLException e) {
                connection.rollback();
                log.debug(e.getMessage());
                throw new DataBaseCustomApplicationException("Database unexpected error.");
            }
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Database connection error.");
        }
    }

    public byte[] fetchContentInPdfByDocumentId(Long documentId) {
        try (Connection connection = dataSource.getConnection()) {
            log.debug(String.format("Try to find Content in PDF for Document with id = %d", documentId));
            String getContentInPdfSql =
                    "SELECT content_in_pdf " +
                            "FROM bpm_document " +
                            "WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(getContentInPdfSql)) {
                pstmt.setLong(1, documentId);
                try (ResultSet resultSet = pstmt.executeQuery()) {
                    if (resultSet.next()) {
                        log.debug(String.format("Content in Pdf for Document with id = %d found", documentId));
                        return resultSet.getBytes(1);
                    }
                    String message = String.format("Document with id= %d not found", documentId);
                    log.debug(message);
                    throw new DataBaseCustomApplicationException(message);
                }
            }
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Database connection error.");
        }
    }

    public byte[] fetchDocumentInPdfByDocumentId(Long documentId) {
        try (Connection connection = dataSource.getConnection()) {
            log.debug(String.format("Try to find Document in PDF for Document with id = %d", documentId));
            String getContentInPdfSql =
                    "SELECT document_in_pdf " +
                            "FROM bpm_document " +
                            "WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(getContentInPdfSql)) {
                pstmt.setLong(1, documentId);
                try (ResultSet resultSet = pstmt.executeQuery()) {
                    if (resultSet.next()) {
                        log.debug(String.format("Document in Pdf for Document with id = %d found", documentId));
                        return resultSet.getBytes(1);
                    }
                    String message = String.format("Document with id= %d not found", documentId);
                    log.debug(message);
                    throw new DataBaseCustomApplicationException(message);
                }
            }
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Database connection error.");
        }
    }
}
