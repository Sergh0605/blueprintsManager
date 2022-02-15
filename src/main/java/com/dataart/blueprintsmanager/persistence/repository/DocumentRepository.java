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
                    .designer(getUser((Long) resultSet.getObject("designerId"), connection))
                    .supervisor(getUser((Long) resultSet.getObject("supervisorId"), connection))
                    .reassemblyRequired(resultSet.getBoolean("reassembly"))
                    .editTime(resultSet.getTimestamp("editTime").toLocalDateTime())
                    .build();
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Can't parse Document object from DB");
        }
    }

    private UserEntity getUser(Long userId, Connection connection) {
        return Optional.ofNullable(userId).map(x -> userRepository.fetchById(x, connection, true)).orElse(null);
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
            try {
                DocumentEntity documentEntity = fetchById(documentId, connection);
                connection.commit();
                log.debug(String.format("Document with id = %d found", documentId));
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
                Integer maxDocNumber = fetchMaxDocNumberByProjectId(document, connection);
                if (maxDocNumber == null) {
                    document.setNumberInProject(1);
                } else document.setNumberInProject(maxDocNumber + 1);
                Long createdDocumentId = create(document, connection);
                DocumentEntity documentEntity = fetchById(createdDocumentId, connection);
                projectRepository.setReassemblyRequiredById(documentEntity.getProject().getId(), connection);
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

    private Integer fetchMaxDocNumberByProjectId(DocumentEntity document, Connection connection) throws SQLException {
        String getMaxDocNumberSql =
                "SELECT max(number_in_project) as maxNumber " +
                        "FROM bpm_document " +
                        "WHERE deleted = false AND project_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(getMaxDocNumberSql)) {
            pstmt.setLong(1, document.getProject().getId());
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    Integer maxDocNumber = (Integer) resultSet.getObject("maxNumber");
                    log.debug(String.format("Max Document number in Project with id = %d is %d", document.getProject().getId(), maxDocNumber));
                    return maxDocNumber;
                }
                log.debug(String.format("Project with id= %d hasn't Documents", document.getProject().getId()));
                return null;
            }
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

    public DocumentEntity updateDocumentInPdfTransactional(Long documentId, byte[] documentInPdf) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                log.debug(String.format("Try to update Document file in PDF with id = %d", documentId));
                updateDocumentInPdf(documentId, documentInPdf, connection);
                DocumentEntity documentEntity = fetchById(documentId, connection);
                projectRepository.setReassemblyRequiredById(documentEntity.getProject().getId(), connection);
                connection.commit();
                log.debug(String.format("Document in PDF updated in Document with id = %d", documentId));
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

    private void updateDocumentInPdf(Long documentId, byte[] documentInPdf, Connection connection) throws SQLException {
        String updateFileInDocumentByIdSql =
                "UPDATE  bpm_document SET document_in_pdf = ?, reassembly_required = false " +
                        "WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(updateFileInDocumentByIdSql)) {
            pstmt.setBytes(1, documentInPdf);
            pstmt.setLong(2, documentId);
            pstmt.executeUpdate();
        }
    }

    public byte[] fetchContentInPdfByDocumentIdTransactional(Long documentId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                return fetchContentInPdfByDocumentId(documentId, connection);
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

    private byte[] fetchContentInPdfByDocumentId(Long documentId, Connection connection) throws SQLException {
        log.debug(String.format("Try to find Content in PDF for Document with id = %d", documentId));
        String getContentInPdfSql =
                "SELECT content_in_pdf as contentInPdf " +
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
    }

    public byte[] fetchDocumentInPdfByDocumentIdTransactional(Long documentId) {
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

    public void deleteProjectTransactional(Long projectId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                log.debug(String.format("Try to delete project with id = %d", projectId));
                log.debug(String.format("Try to set deleted Documents with Project id = %d", projectId));
                int countOfDeletedDocuments = deleteByProjectId(projectId, connection);
                int countOfDeletedProjects = projectRepository.deleteById(projectId, connection);
                connection.commit();
                log.debug(String.format("%d Documents with Project id = %d set deleted", countOfDeletedDocuments, projectId));
                log.debug(String.format("%d Project with id = %d set deleted", countOfDeletedProjects, projectId));
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

    private int deleteByProjectId(Long projectId, Connection connection) {
        String setDeleteByProjectIdSql =
                "UPDATE  bpm_document SET deleted = true, reassembly_required = false " +
                        "WHERE project_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(setDeleteByProjectIdSql)) {
            pstmt.setLong(1, projectId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Long deleteByIdTransactional(Long documentId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                log.debug(String.format("Try to delete document with id = %d", documentId));
                Long projectId = fetchById(documentId, connection).getProject().getId();
                deleteById(documentId, connection);
                projectRepository.setReassemblyRequiredById(projectId, connection);
                connection.commit();
                log.debug(String.format("Document with id = %d set deleted", documentId));
                return projectId;
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

    private void deleteById(Long documentId, Connection connection) {
        String setDeleteByProjectIdSql =
                "UPDATE  bpm_document SET deleted = true, reassembly_required = false " +
                        "WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(setDeleteByProjectIdSql)) {
            pstmt.setLong(1, documentId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public DocumentEntity updateTransactional(DocumentEntity updateEntity) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                log.debug(String.format("Try to update document with id = %d", updateEntity.getId()));
                if (updateEntity.getContentInPdf() == null) {
                    updateEntity.setContentInPdf(fetchContentInPdfByDocumentId(updateEntity.getId(), connection));
                }
                update(updateEntity, connection);
                DocumentEntity updatedEntity = fetchById(updateEntity.getId(), connection);
                projectRepository.setReassemblyRequiredById(updatedEntity.getProject().getId(), connection);
                connection.commit();
                log.debug(String.format("Document with id = %d updated", updateEntity.getId()));
                return updatedEntity;
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

    private void update(DocumentEntity document, Connection connection) throws SQLException {
        String updateDocumentByIdSql =
                "UPDATE  bpm_document SET number_in_project = ?, type_id = ?, name = ?, code = ?, designer_id = ?, " +
                        "supervisor_id = ?, content_in_pdf = ?, reassembly_required = true, edit_time = ? " +
                        "WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(updateDocumentByIdSql)) {
            pstmt.setLong(1, document.getNumberInProject());
            pstmt.setLong(2, document.getDocumentType().getId());
            pstmt.setString(3, document.getName());
            pstmt.setString(4, document.getCode());
            pstmt.setObject(5, Optional.ofNullable(document.getDesigner()).map(UserEntity::getId).orElse(null));
            pstmt.setObject(6, Optional.ofNullable(document.getDesigner()).map(UserEntity::getId).orElse(null));
            pstmt.setObject(7, document.getContentInPdf());
            pstmt.setTimestamp(8, Timestamp.valueOf(document.getEditTime()));
            pstmt.setLong(9, document.getId());
            pstmt.executeUpdate();
        }
    }
}
