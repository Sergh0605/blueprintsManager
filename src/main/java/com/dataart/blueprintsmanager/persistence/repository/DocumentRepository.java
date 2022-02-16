package com.dataart.blueprintsmanager.persistence.repository;

import com.dataart.blueprintsmanager.exceptions.CustomApplicationException;
import com.dataart.blueprintsmanager.exceptions.DataBaseCustomApplicationException;
import com.dataart.blueprintsmanager.exceptions.NotFoundCustomApplicationException;
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

    public List<DocumentEntity> fetchAllByProjectId(Long projectId) {
        log.info(String.format("Try to find Documents for Project with id = %d", projectId));
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
            try (ResultSet resultSet = pstmt.executeQuery()) {
                Integer documentsCount = 0;
                List<DocumentEntity> documentEntityList = new ArrayList<>();
                while (resultSet.next()) {
                    DocumentEntity document = buildDocument(resultSet, connection);
                    documentEntityList.add(document);
                }
                log.info(String.format("%d Documents found for Project with id = %d", documentEntityList.size(), projectId));
                return documentEntityList;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataBaseCustomApplicationException("Database connection error.", e);
        }
    }

    public DocumentEntity fetchById(Long documentId) {
        try (Connection connection = dataSource.getConnection()) {
            return fetchById(documentId, connection);
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw new DataBaseCustomApplicationException("Database unexpected error.", e);
        } catch (CustomApplicationException e) {
            log.info(e.getMessage());
            throw e;
        }
    }

    public DocumentEntity createTransactional(DocumentEntity document) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                log.info(String.format("Try to transactional create new %s Document for Project with id = %d", document.getDocumentType().name(), document.getProject().getId()));
                Integer maxDocNumber = fetchMaxDocNumberByProjectId(document, connection);
                if (maxDocNumber == null) {
                    document.setNumberInProject(1);
                } else document.setNumberInProject(maxDocNumber + 1);
                Long createdDocumentId = create(document, connection);
                DocumentEntity documentEntity = fetchById(createdDocumentId, connection);
                setReassemblyRequiredForContentsByProjectId(documentEntity.getProject().getId(), connection);
                projectRepository.setReassemblyRequiredById(documentEntity.getProject().getId(), connection);
                connection.commit();
                log.info(String.format("Document with id = %d is transactional created", documentEntity.getId()));
                return documentEntity;
            } catch (SQLException e) {
                connection.rollback();
                log.error(e.getMessage(), e);
                throw new DataBaseCustomApplicationException("Database unexpected error.", e);
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

    public void updateContentInPdfByDocumentId(Long documentId, byte[] documentInPdf) {
        String updateContentInDocumentByIdSql =
                "UPDATE  bpm_document SET content_in_pdf = ? " +
                        "WHERE id = ?";
        try (Connection connection = dataSource.getConnection()) {
            log.info(String.format("Try to update document content in PDF with id = %d", documentId));
            try (PreparedStatement pstmt = connection.prepareStatement(updateContentInDocumentByIdSql)) {
                pstmt.setBytes(1, documentInPdf);
                pstmt.setLong(2, documentId);
                int countOfAffectedRows = pstmt.executeUpdate();
                if (countOfAffectedRows > 0) {
                    log.info(String.format("Content in PDF for Document with id = %d updated", documentId));
                } else {
                    String message = String.format("Content in PDF updated in Document with id = %d", documentId);
                    log.info(message);
                    throw new NotFoundCustomApplicationException(message);
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataBaseCustomApplicationException("Database connection error.", e);
        }
    }

    public DocumentEntity updateDocumentInPdfByDocumentIdTransactional(Long documentId, byte[] documentInPdf) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                log.info(String.format("Try to transactional update Document in PDF with id = %d", documentId));
                updateDocumentInPdf(documentId, documentInPdf, connection);
                DocumentEntity documentEntity = fetchById(documentId, connection);
                projectRepository.setReassemblyRequiredById(documentEntity.getProject().getId(), connection);
                connection.commit();
                log.info(String.format("Document in PDF transactional updated in Document with id = %d", documentId));
                return documentEntity;
            } catch (SQLException e) {
                connection.rollback();
                log.debug(e.getMessage(), e);
                throw new DataBaseCustomApplicationException("Database unexpected error.", e);
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

    public byte[] fetchContentInPdfByDocumentId(Long documentId) {
        try (Connection connection = dataSource.getConnection()) {
                return fetchContentInPdfByDocumentId(documentId, connection);
            } catch (SQLException e) {
                log.debug(e.getMessage(), e);
                throw new DataBaseCustomApplicationException("Database unexpected error.", e);
            } catch (CustomApplicationException e) {
                log.info(e.getMessage(), e);
                throw e;
            }
    }

    public byte[] fetchDocumentInPdfByDocumentId(Long documentId) {
        try (Connection connection = dataSource.getConnection()) {
            log.info(String.format("Try to find Document in PDF for Document with id = %d", documentId));
            String getContentInPdfSql =
                    "SELECT document_in_pdf " +
                            "FROM bpm_document " +
                            "WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(getContentInPdfSql)) {
                pstmt.setLong(1, documentId);
                try (ResultSet resultSet = pstmt.executeQuery()) {
                    if (resultSet.next()) {
                        byte[] documentInPdf = resultSet.getBytes(1);
                        if (documentInPdf != null && documentInPdf.length > 0) {
                            log.info(String.format("Document in Pdf for Document with id = %d found", documentId));
                            return documentInPdf;
                        }
                    }
                    String message = String.format("Document with id= %d not found", documentId);
                    log.info(message);
                    throw new NotFoundCustomApplicationException(message);
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataBaseCustomApplicationException("Database connection error.", e);
        }
    }

    public Long deleteByIdTransactional(Long documentId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                log.info(String.format("Try to transactional delete document with id = %d", documentId));
                Long projectId = fetchById(documentId, connection).getProject().getId();
                deleteById(documentId, connection);
                setReassemblyRequiredForContentsByProjectId(projectId, connection);
                projectRepository.setReassemblyRequiredById(projectId, connection);
                connection.commit();
                log.info(String.format("Document with id = %d set transactional deleted", documentId));
                return projectId;
            } catch (SQLException e) {
                connection.rollback();
                log.error(e.getMessage(), e);
                throw new DataBaseCustomApplicationException("Database unexpected error.", e);
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

    public DocumentEntity updateTransactional(DocumentEntity updateEntity) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                log.info(String.format("Try to transactional update document with id = %d", updateEntity.getId()));
                if (updateEntity.getContentInPdf() == null) {
                    updateEntity.setContentInPdf(fetchContentInPdfByDocumentId(updateEntity.getId(), connection));
                }
                update(updateEntity, connection);
                DocumentEntity updatedEntity = fetchById(updateEntity.getId(), connection);
                setReassemblyRequiredForContentsByProjectId(updatedEntity.getProject().getId(), connection);
                projectRepository.setReassemblyRequiredById(updatedEntity.getProject().getId(), connection);
                connection.commit();
                log.info(String.format("Document with id = %d transactional updated", updateEntity.getId()));
                return updatedEntity;
            } catch (SQLException e) {
                connection.rollback();
                log.error(e.getMessage(), e);
                throw new DataBaseCustomApplicationException("Database unexpected error.", e);
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

    private DocumentEntity fetchById(Long documentId, Connection connection) throws SQLException {
        log.info(String.format("Try to find Document with id = %d", documentId));
        String getDocumentByIdSql =
                "SELECT id, project_id as projectId, number_in_project as number, type_id as typeId, name, code, " +
                        "designer_id as designerId, supervisor_id as supervisorId, reassembly_required as reassembly, edit_time as editTime " +
                        "FROM bpm_document " +
                        "WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(getDocumentByIdSql)) {
            pstmt.setLong(1, documentId);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    log.info(String.format("Document with id = %d found", documentId));
                    return buildDocument(resultSet, connection);
                }
                throw new NotFoundCustomApplicationException(String.format("Document with id= %d not found", documentId));
            }
        }
    }

    private Integer fetchMaxDocNumberByProjectId(DocumentEntity document, Connection connection) throws SQLException {
        log.info(String.format("Try to fetch max number of Document in Project with id = %d", document.getProject().getId()));
        String getMaxDocNumberSql =
                "SELECT max(number_in_project) as maxNumber " +
                        "FROM bpm_document " +
                        "WHERE deleted = false AND project_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(getMaxDocNumberSql)) {
            pstmt.setLong(1, document.getProject().getId());
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    Integer maxDocNumber = (Integer) resultSet.getObject("maxNumber");
                    log.info(String.format("Max Document number in Project with id = %d is %d", document.getProject().getId(), maxDocNumber));
                    return maxDocNumber;
                }
                log.info(String.format("Project with id= %d hasn't Documents", document.getProject().getId()));
                return null;
            }
        }
    }

    private Long create(DocumentEntity document, Connection connection) throws SQLException {
        log.info(String.format("Try to create new Document with type = %s", document.getDocumentType()));
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
            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long key = generatedKeys.getLong(1);
                    log.info(String.format("New Document with id = %d created", key));
                    return key;
                } else {
                    throw new DataBaseCustomApplicationException("Creating document failed, no ID obtained.");
                }
            }
        }
    }

    private void updateDocumentInPdf(Long documentId, byte[] documentInPdf, Connection connection) throws SQLException {
        log.info(String.format("Try to update Document in PDF for Document with id = %d", documentId));
        String updateFileInDocumentByIdSql =
                "UPDATE  bpm_document SET document_in_pdf = ?, reassembly_required = false " +
                        "WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(updateFileInDocumentByIdSql)) {
            pstmt.setBytes(1, documentInPdf);
            pstmt.setLong(2, documentId);
            int countOfAffectedRows = pstmt.executeUpdate();
            if (countOfAffectedRows > 0) {
                log.info(String.format("Document in PDF for Document with id = %d updated", documentId));
            } else {
                throw new NotFoundCustomApplicationException(String.format("Document with id = %d not found", documentId));
            }
        }
    }

    private byte[] fetchContentInPdfByDocumentId(Long documentId, Connection connection) throws SQLException {
        log.info(String.format("Try to find Content in PDF for Document with id = %d", documentId));
        String getContentInPdfSql =
                "SELECT content_in_pdf as contentInPdf " +
                        "FROM bpm_document " +
                        "WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(getContentInPdfSql)) {
            pstmt.setLong(1, documentId);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    log.info(String.format("Content in Pdf for Document with id = %d found", documentId));
                    return resultSet.getBytes(1);
                }
                throw new NotFoundCustomApplicationException(String.format("Document with id= %d not found", documentId));
            }
        }
    }

    private void deleteById(Long documentId, Connection connection) throws SQLException {
        log.info(String.format("Try to delete Document id = %d", documentId));
        String setDeleteByProjectIdSql =
                "UPDATE  bpm_document SET deleted = true, reassembly_required = false " +
                        "WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(setDeleteByProjectIdSql)) {
            pstmt.setLong(1, documentId);
            int countOfAffectedRows = pstmt.executeUpdate();
            if (countOfAffectedRows > 0) {
                log.info(String.format("Document with id = %d deleted", documentId));
            } else {
                throw new NotFoundCustomApplicationException(String.format("Document with id = %d not found", documentId));
            }
        }
    }

    private void update(DocumentEntity document, Connection connection) throws SQLException {
        log.info(String.format("Try to update Document id = %d", document.getId()));
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
            int countOfAffectedRows = pstmt.executeUpdate();
            if (countOfAffectedRows > 0) {
                log.info(String.format("Document with id = %d updated", document.getId()));
            } else {
                throw new NotFoundCustomApplicationException(String.format("Document with id = %d not found", document.getId()));
            }
        }
    }

    private void setReassemblyRequiredForContentsByProjectId(Long projectId, Connection connection) throws SQLException {
        log.info(String.format("Try to set Reassembly required for Content type Document from Project with id = %d", projectId));
        String setReassemblyByProjectIdSql =
                "UPDATE  bpm_document SET reassembly_required = true " +
                        "WHERE project_id = ? AND type_id = 3";
        try (PreparedStatement pstmt = connection.prepareStatement(setReassemblyByProjectIdSql)) {
            pstmt.setLong(1, projectId);
            int countOfAffectedRows = pstmt.executeUpdate();
            if (countOfAffectedRows > 0) {
                log.info(String.format("Reassembly required for Content type Document from Project with id = %d is set", projectId));
            }

        }
    }

    private DocumentEntity buildDocument(ResultSet resultSet, Connection connection) throws SQLException {
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
    }

    private UserEntity getUser(Long userId, Connection connection) {
        return Optional.ofNullable(userId).map(id -> {
            try {
                return userRepository.fetchById(id, connection, true);
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new DataBaseCustomApplicationException("Database Unexpected error", e);
            }
        }).orElse(null);
    }
}
