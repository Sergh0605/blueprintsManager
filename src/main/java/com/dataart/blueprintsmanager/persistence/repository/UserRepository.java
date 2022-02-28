package com.dataart.blueprintsmanager.persistence.repository;

import com.dataart.blueprintsmanager.exceptions.CustomApplicationException;
import com.dataart.blueprintsmanager.exceptions.DataBaseCustomApplicationException;
import com.dataart.blueprintsmanager.persistence.entity.CompanyEntity;
import com.dataart.blueprintsmanager.persistence.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
@AllArgsConstructor
public class UserRepository {
    private final DataSource dataSource;
    private final CompanyRepository companyRepository;

    public List<UserEntity> fetchAll() {
        log.info("Try to find all users");
        String getAllUsersSql =
                "SELECT id, last_name as name, login, password, signature, company_id as companyId, email " +
                        "FROM bpm_user usr " +
                        "WHERE usr.deleted = 'false' " +
                        "ORDER BY name ";
        try (Connection connection = dataSource.getConnection();
             Statement pstmt = connection.createStatement();
             ResultSet resultSet = pstmt.executeQuery(getAllUsersSql)) {
            List<UserEntity> userEntityList = new ArrayList<>();
            while (resultSet.next()) {
                UserEntity user = buildUser(resultSet, connection, false);
                userEntityList.add(user);
            }
            log.info(String.format("%d Users found", userEntityList.size()));
            return userEntityList;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataBaseCustomApplicationException("Database connection error.", e);
        }
    }

    public UserEntity fetchById(Long userId) {
        try (Connection connection = dataSource.getConnection()) {
            return fetchById(userId, connection, true);
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw new DataBaseCustomApplicationException("Database unexpected error.", e);
        } catch (CustomApplicationException e) {
            log.info(e.getMessage());
            throw e;
        }
    }

    public UserEntity fetchByLogin(String login) {
        try (Connection connection = dataSource.getConnection()) {
            return fetchByLogin(login, connection);
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw new DataBaseCustomApplicationException("Database unexpected error.", e);
        } catch (CustomApplicationException e) {
            log.info(e.getMessage());
            throw e;
        }
    }

    public List<UserEntity> fetchAllByCompanyId(Long companyId) {
        log.info(String.format("Try to find all users for company with id = %d", companyId));
        String getAllUsersSql =
                "SELECT id as id, last_name as name, login, password, signature, company_id as companyId, email " +
                        "FROM bpm_user " +
                        "WHERE company_id = ? AND deleted = 'false' " +
                        "ORDER BY name ";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(getAllUsersSql)) {
            pstmt.setLong(1, companyId);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                List<UserEntity> userEntityList = new ArrayList<>();
                while (resultSet.next()) {
                    UserEntity user = buildUser(resultSet, connection, true);
                    userEntityList.add(user);
                }
                log.debug(String.format("%d Users found for Company with companyId = %d", userEntityList.size(), companyId));
                return userEntityList;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataBaseCustomApplicationException("Database connection error.", e);
        }
    }

    protected UserEntity fetchById(Long userId, Connection connection, boolean lazyInitialization) throws SQLException {
        log.info(String.format("Try to find user with id = %d", userId));
        String getUserByIdSql =
                "SELECT id, last_name as name, login, password, company_id as companyId, signature, email " +
                        "FROM bpm_user " +
                        "WHERE deleted = false AND  id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(getUserByIdSql)) {
            pstmt.setObject(1, userId);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    log.debug(String.format("User with id = %d found", userId));
                    return buildUser(resultSet, connection, lazyInitialization);
                }
                String message = String.format("User with userId= %d not found", userId);
                log.info(message);
                return null;
            }
        }
    }

    private UserEntity buildUser(ResultSet resultSet, Connection connection, boolean lazyInitialization) throws SQLException {
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
                .email(resultSet.getString("email"))
                .company(company)
                .build();
    }

    protected UserEntity fetchByLogin(String login, Connection connection) throws SQLException {
        log.info(String.format("Try to find user with Login = %s", login));
        String getUserByLoginSql =
                "SELECT id, last_name as name, login, password, company_id as companyId, signature, email " +
                        "FROM bpm_user " +
                        "WHERE deleted = 'false' AND  login = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(getUserByLoginSql)) {
            pstmt.setObject(1, login);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    log.debug(String.format("User with Login = %s found", login));
                    return buildUser(resultSet, connection, true);
                }
                String message = String.format("User with Login = %s not found", login);
                log.info(message);
                throw new DataBaseCustomApplicationException(message);
            }
        }
    }
}
