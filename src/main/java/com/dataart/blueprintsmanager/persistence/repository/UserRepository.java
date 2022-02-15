package com.dataart.blueprintsmanager.persistence.repository;

import com.dataart.blueprintsmanager.exceptions.DataBaseCustomApplicationException;
import com.dataart.blueprintsmanager.persistence.entity.CompanyEntity;
import com.dataart.blueprintsmanager.persistence.entity.UserEntity;
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
public class UserRepository {
    private final DataSource dataSource;
    private final CompanyRepository companyRepository;

    private UserEntity buildUser(ResultSet resultSet, Connection connection, boolean lazyInitialization) {
        try {
            CompanyEntity company = null;
            if (!lazyInitialization) {
                company = companyRepository.fetchById(resultSet.getLong("companyId"), connection);
            }
            return UserEntity.builder()
                    .id(resultSet.getLong("id"))
                    .lastName(resultSet.getString("name"))
                    .login(resultSet.getString("login"))
                    .password(resultSet.getString("password"))
                    .signature(resultSet.getBytes("signature"))
                    .company(company)
                    .build();
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Can't parse user object from DB");
        }
    }

    public UserEntity fetchByIdTransactional(Long userId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            UserEntity userEntity = fetchById(userId, connection, true);
            connection.commit();
            return userEntity;
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Database connection error.");
        }
    }

    protected UserEntity fetchById(Long userId, Connection connection, boolean lazyInitialization) {
        String getCompanyByIdSql =
                "SELECT id, last_name as name, login, password, company_id as companyId, signature " +
                        "FROM bpm_user " +
                        "WHERE deleted = 'false' AND  id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(getCompanyByIdSql)) {
            pstmt.setLong(1, userId);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    log.debug(String.format("User with id = %d found", userId));
                    return buildUser(resultSet, connection, lazyInitialization);
                }
                String message = String.format("User with userId= %d not found", userId);
                log.debug(message);
                throw new DataBaseCustomApplicationException(message);
            }
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Database connection error.");
        }
    }


    public List<UserEntity> fetchAllTransactional() {
        String getAllUsersSql =
                "SELECT id, last_name as name, login, password, signature, company_id as companyId " +
                        "FROM bpm_user usr " +
                        "WHERE usr.deleted = 'false' " +
                        "ORDER BY name ";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(getAllUsersSql);
             ResultSet resultSet = pstmt.executeQuery()) {
            List<UserEntity> userEntityList = new ArrayList<>();
            Integer usersCount = 0;
            while (resultSet.next()) {
                UserEntity user = buildUser(resultSet, connection, false);
                userEntityList.add(user);
                usersCount++;
            }
            log.debug(String.format("%d Users found", usersCount));
            return userEntityList;
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Database connection error.");
        }
    }

    public List<UserEntity> fetchAllByCompanyId(Long companyId) {
        String getAllUsersSql =
                "SELECT id as id, last_name as name, login, password, signature, company_id as companyId " +
                        "FROM bpm_user " +
                        "WHERE company_id = ? AND deleted = 'false' " +
                        "ORDER BY name ";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(getAllUsersSql)
        ) {
            pstmt.setLong(1, companyId);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                List<UserEntity> userEntityList = new ArrayList<>();
                Integer usersCount = 0;
                while (resultSet.next()) {
                    UserEntity user = buildUser(resultSet, connection, true);
                    userEntityList.add(user);
                    usersCount++;
                }
                log.debug(String.format("%d Users found for Company with companyId = %d", usersCount, companyId));
                return userEntityList;
            }
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Database connection error.");
        }
    }
}
