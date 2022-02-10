package com.dataart.blueprintsmanager.persistence.repository;

import com.dataart.blueprintsmanager.exceptions.DataBaseCustomApplicationException;
import com.dataart.blueprintsmanager.persistence.entity.DocumentEntity;
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

    public int setReassemblyRequiredByProjectId(Long projectId, Connection connection) throws SQLException {
        String updateProjectByIdSql =
                "UPDATE  bpm_document SET reassembly_required = true " +
                        "WHERE project_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(updateProjectByIdSql)) {
            pstmt.setLong(1, projectId);
            return pstmt.executeUpdate();
        }
    }

    public List<DocumentEntity> findAllByProjectId(Long projectId) {
        String getByProjectIdSql =
                "SELECT doc.id as id, doc.number_in_project as number, doc.code as code, doc.name as name, doc.reassembly_required as reassembly, doc.edit_time as editTime " +
                        "FROM bpm_document doc " +
                        "WHERE doc.deleted = 'false' AND doc.project_id = ? " +
                        "ORDER BY doc.number_in_project";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(getByProjectIdSql);
        ) {
            pstmt.setLong(1, projectId);
            log.debug(String.format("Try to find Documents for Project with id = %d", projectId));
            try (ResultSet resultSet = pstmt.executeQuery()) {
                Integer documentsCount = 0;
                List<DocumentEntity> documentEntityList = new ArrayList<>();
                while (resultSet.next()) {
                    DocumentEntity document = DocumentEntity.builder()
                            .id(resultSet.getLong("id"))
                            .numberInProject(resultSet.getInt("number"))
                            .code(resultSet.getString("code"))
                            .name(resultSet.getString("name"))
                            .reassemblyRequired(resultSet.getBoolean("reassembly"))
                            .editTime(resultSet.getTimestamp("editTime").toLocalDateTime())
                            .build();
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
}
