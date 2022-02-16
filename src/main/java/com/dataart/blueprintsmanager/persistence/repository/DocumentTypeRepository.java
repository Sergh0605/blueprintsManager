package com.dataart.blueprintsmanager.persistence.repository;

import com.dataart.blueprintsmanager.exceptions.DataBaseCustomApplicationException;
import com.dataart.blueprintsmanager.exceptions.NotFoundCustomApplicationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
@Slf4j
@AllArgsConstructor
public class DocumentTypeRepository {
    private final DataSource dataSource;

    public byte[] fetchFirstPageTemplateById(Long typeId) {
        log.info(String.format("Try to find First Page Template for type with id = %d", typeId));
        String getTemplatesByTypeIdSql =
                "SELECT first_page_template as firstPage " +
                        "FROM bpm_document_type " +
                        "WHERE id = ? ";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(getTemplatesByTypeIdSql)
        ) {
            pstmt.setLong(1, typeId);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    byte[] generalPage = resultSet.getBytes("firstPage");
                    if (generalPage != null && generalPage.length > 0) {
                        log.info(String.format("First Page Template found for Document Type with id = %d", typeId));
                        return generalPage;
                    }
                }
                String message = String.format("First Page Template for Document Type with id= %d not found", typeId);
                log.info(message);
                throw new NotFoundCustomApplicationException(message);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataBaseCustomApplicationException("Database connection error.", e);
        }
    }

    public byte[] fetchGeneralPageTemplateById(Long typeId) {
        log.info(String.format("Try to find General Page Template for type with id = %d", typeId));
        String getTemplatesByTypeIdSql =
                "SELECT general_page_template as generalPage " +
                        "FROM bpm_document_type " +
                        "WHERE id = ? ";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(getTemplatesByTypeIdSql)) {
            pstmt.setLong(1, typeId);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    byte[] generalPage = resultSet.getBytes("generalPage");
                    if (generalPage != null && generalPage.length > 0) {
                        log.info(String.format("General Page Template found for Document Type with id = %d", typeId));
                        return generalPage;
                    }
                }
                String message = String.format("General Page Template for Document Type with id= %d not found", typeId);
                log.info(message);
                throw new NotFoundCustomApplicationException(message);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataBaseCustomApplicationException("Database connection error.", e);
        }
    }

    // TODO: 18.02.2022 delete after the main functionality is completed
    public void updateTemplate(Long typeId, byte[] firstPage, byte[] secPage) {
        try (Connection connection = dataSource.getConnection()) {
            updateTemplateInPdf(typeId, firstPage, secPage, connection);
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Database connection error.");
        }
    }

    private void updateTemplateInPdf(Long typeId, byte[] firstPage, byte[] secPage, Connection connection) throws SQLException {
        String updateFileInDocumentByIdSql =
                "UPDATE  bpm_document_type SET first_page_template = ?, general_page_template = ? " +
                        "WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(updateFileInDocumentByIdSql)) {
            pstmt.setBytes(1, firstPage);
            pstmt.setBytes(2, secPage);
            pstmt.setLong(3, typeId);
            pstmt.executeUpdate();
        }
    }
}
