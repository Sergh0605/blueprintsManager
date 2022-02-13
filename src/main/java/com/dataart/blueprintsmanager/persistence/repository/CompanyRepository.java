package com.dataart.blueprintsmanager.persistence.repository;

import com.dataart.blueprintsmanager.exceptions.DataBaseCustomApplicationException;
import com.dataart.blueprintsmanager.persistence.entity.CompanyEntity;
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
public class CompanyRepository {
    private final DataSource dataSource;

    private CompanyEntity buildCompany(ResultSet resultSet) {
        try {
            return CompanyEntity.builder()
                    .id(resultSet.getLong("id"))
                    .name(resultSet.getString("name"))
                    .signerPosition(resultSet.getString("signerPosition"))
                    .signerName(resultSet.getString("signerName"))
                    .logo(resultSet.getBytes("logo"))
                    .city(resultSet.getString("city"))
                    .build();
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Can't parse company object from DB");
        }
    }

    public CompanyEntity fetchByIdTransactional(Long companyId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            CompanyEntity companyEntity = fetchById(companyId, connection);
            connection.commit();

            return companyEntity;
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Database connection error.");
        }
    }

    public List<CompanyEntity> fetchAllTransactional() {
        String getAllCompaniesSql =
                "SELECT id, name, signer_position as signerPosition, signer_name as signerName, encode(logo, 'escape') as logo, city " +
                        "FROM bpm_company " +
                        "WHERE deleted = 'false'";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(getAllCompaniesSql);
             ResultSet resultSet = pstmt.executeQuery()) {
            List<CompanyEntity> companyEntityList = new ArrayList<>();
            Integer companiesCount = 0;
            while (resultSet.next()) {
                CompanyEntity company = buildCompany(resultSet);
                companyEntityList.add(company);
                companiesCount++;
            }
            log.debug(String.format("%d Companies found", companiesCount));
            return companyEntityList;
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Database connection error.");
        }
    }

    protected CompanyEntity fetchById(Long companyId, Connection connection) {
        String getCompanyByIdSql =
                "SELECT id, name, signer_position as signerPosition, signer_name as signerName, encode(logo, 'escape') as logo, city " +
                        "FROM bpm_company " +
                        "WHERE deleted = 'false' AND  id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(getCompanyByIdSql)) {
            pstmt.setLong(1, companyId);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    log.debug(String.format("Company with id = %d found", companyId));
                    return buildCompany(resultSet);
                }
                String message = String.format("Company with companyId= %d not found", companyId);
                log.debug(message);
                throw new DataBaseCustomApplicationException(message);
            }
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Database connection error.");
        }
    }
}

