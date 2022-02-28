package com.dataart.blueprintsmanager.persistence.repository;

import com.dataart.blueprintsmanager.exceptions.CustomApplicationException;
import com.dataart.blueprintsmanager.exceptions.DataBaseCustomApplicationException;
import com.dataart.blueprintsmanager.exceptions.NotFoundCustomApplicationException;
import com.dataart.blueprintsmanager.persistence.entity.CompanyEntity;
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
public class CompanyRepository {
    private final DataSource dataSource;

    public CompanyEntity fetchById(Long companyId) {
        try (Connection connection = dataSource.getConnection()) {
            return fetchById(companyId, connection);
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw new DataBaseCustomApplicationException("Database connection error.");
        } catch (CustomApplicationException e) {
            log.info(e.getMessage());
            throw e;
        }
    }

    protected CompanyEntity fetchByNullableId(Long companyId, Connection connection) {
        return Optional.ofNullable(companyId).map(x -> {
            try {
                return fetchById(x, connection);
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new DataBaseCustomApplicationException("Database Unexpected error", e);
            }
        }).orElse(null);
    }

    public List<CompanyEntity> fetchAll() {
        log.info("Try to find All Companies");
        String getAllCompaniesSql =
                "SELECT id, name, signer_position as signerPosition, signer_name as signerName, logo, city " +
                        "FROM bpm_company " +
                        "WHERE deleted = 'false'";
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet resultSet = stmt.executeQuery(getAllCompaniesSql)) {
            List<CompanyEntity> companyEntityList = new ArrayList<>();
            while (resultSet.next()) {
                CompanyEntity company = buildCompany(resultSet);
                companyEntityList.add(company);
            }
            log.info(String.format("%d Companies found", companyEntityList.size()));
            return companyEntityList;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataBaseCustomApplicationException("Database connection error.", e);
        }
    }

    protected CompanyEntity fetchById(Long companyId, Connection connection) throws SQLException {
        log.info(String.format("Try to find Company with id = %d", companyId));
        String getCompanyByIdSql =
                "SELECT id, name, signer_position as signerPosition, signer_name as signerName, logo, city " +
                        "FROM bpm_company " +
                        "WHERE deleted = 'false' AND  id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(getCompanyByIdSql)) {
            pstmt.setLong(1, companyId);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    log.info(String.format("Company with id = %d found", companyId));
                    return buildCompany(resultSet);
                }
                throw new NotFoundCustomApplicationException(String.format("Company with companyId= %d not found", companyId));
            }
        }
    }

    private CompanyEntity buildCompany(ResultSet resultSet) throws SQLException {
        return CompanyEntity.builder()
                .id(resultSet.getLong("id"))
                .name(resultSet.getString("name"))
                .signerPosition(resultSet.getString("signerPosition"))
                .signerName(resultSet.getString("signerName"))
                .logo(resultSet.getBytes("logo"))
                .city(resultSet.getString("city"))
                .build();
    }
}

