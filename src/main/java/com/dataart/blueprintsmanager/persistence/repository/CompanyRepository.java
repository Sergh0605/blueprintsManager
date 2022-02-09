package com.dataart.blueprintsmanager.persistence.repository;

import com.dataart.blueprintsmanager.exceptions.DataBaseCustomApplicationException;
import com.dataart.blueprintsmanager.persistence.entity.CompanyEntity;
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
public class CompanyRepository {
    private final DataSource dataSource;

    public CompanyRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<CompanyEntity> findAll() {
        String getAllCompaniesSql =
                "SELECT *" +
                        "FROM bpm_company " +
                        "WHERE deleted = 'false'";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(getAllCompaniesSql);
             ResultSet resultSet = pstmt.executeQuery()) {
            List<CompanyEntity> companyEntityList = new ArrayList<>();
            while (resultSet.next()) {
                CompanyEntity company = CompanyEntity.builder()
                        .id(resultSet.getLong("id"))
                        .name(resultSet.getString("name"))
                        .signerPosition(resultSet.getString("signer_position"))
                        .signerName(resultSet.getString("signer_name"))
                        .logo(resultSet.getBytes("logo"))
                        .build();
                companyEntityList.add(company);
            }
            return companyEntityList;
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Database connection error.");
        }
    }

    public CompanyEntity findById(Long id) {
        String getCompanyByIdSql =
                "SELECT *" +
                        "FROM bpm_company " +
                        "WHERE deleted = 'false' AND  id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(getCompanyByIdSql)) {
            pstmt.setLong(1, id);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    return CompanyEntity.builder()
                            .id(resultSet.getLong("id"))
                            .name(resultSet.getString("name"))
                            .signerPosition(resultSet.getString("signer_position"))
                            .signerName(resultSet.getString("signer_name"))
                            .logo(resultSet.getBytes("logo"))
                            .build();
                }
                String message = String.format("Company with id= %d not found", id);
                log.debug(message);
                throw new DataBaseCustomApplicationException(message);
            }
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Database connection error.");
        }
    }
}

