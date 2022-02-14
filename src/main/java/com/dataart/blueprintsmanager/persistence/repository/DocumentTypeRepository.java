package com.dataart.blueprintsmanager.persistence.repository;

import com.dataart.blueprintsmanager.exceptions.DataBaseCustomApplicationException;
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
public class DocumentTypeRepository {
    private final DataSource dataSource;

    public List<byte[]> fetchPdfTemplatesByIdTransactional(Long typeId) {
        String getTemplatesByTypeIdSql =
                "SELECT first_page_template as firstPage , general_page_template as generalPage " +
                        "FROM bpm_document_type " +
                        "WHERE id = ? ";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(getTemplatesByTypeIdSql)
        ) {
            pstmt.setLong(1, typeId);
            log.debug(String.format("Try to find Templates for Document Type with id = %d", typeId));
            try (ResultSet resultSet = pstmt.executeQuery()) {
                List<byte[]> templatesList = new ArrayList<>();
                if (resultSet.next()) {
                    byte[] firstPage = resultSet.getBytes("firstPage");
                    templatesList.add(firstPage);
                    byte[] generalPage = resultSet.getBytes("generalPage");
                    templatesList.add(generalPage);
                    log.debug(String.format("Templates found for Document Type with id = %d", typeId));
                    return templatesList;
                }
                String message = String.format("Templates for Document Type with id= %d not found", typeId);
                log.debug(message);
                throw new DataBaseCustomApplicationException(message);
            }
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Database connection error.");
        }
    }

    public void updateTemplateTransactional(Long typeId, byte[] firstPage, byte[] secPage) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                updateTemplateInPdf(typeId, firstPage, secPage, connection);
                connection.commit();
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