package com.dataart.blueprintsmanager.persistence.repository;

import com.dataart.blueprintsmanager.exceptions.DataBaseCustomApplicationException;
import com.dataart.blueprintsmanager.persistence.entity.CompanyEntity;
import com.dataart.blueprintsmanager.persistence.entity.UserEntity;
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
public class UserRepository {
    private final DataSource dataSource;

    public UserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<UserEntity> findAll() {
        String getAllUsersSql =
                "SELECT usr.id as id, usr.last_name as name, usr.login as login, usr.signature as sign, cmp.name as cmpname " +
                        "FROM bpm_user usr " +
                        "INNER JOIN bpm_company cmp ON usr.company_id = cmp.id " +
                        "WHERE usr.deleted = 'false' " +
                        "ORDER BY name ";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(getAllUsersSql);
             ResultSet resultSet = pstmt.executeQuery()) {
            List<UserEntity> userEntityList = new ArrayList<>();
            while (resultSet.next()) {
                UserEntity user = UserEntity.builder()
                        .id(resultSet.getLong("id"))
                        .lastName(resultSet.getString("name"))
                        .login(resultSet.getString("login"))
                        .signature(resultSet.getBytes("sign"))
                        .company(CompanyEntity.builder().name(resultSet.getString("cmpname")).build())
                        .build();
                userEntityList.add(user);
            }
            return userEntityList;
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Database connection error.");
        }
    }

    public List<UserEntity> findAllByCompanyId(Long id) {
        String getAllUsersSql =
                "SELECT usr.id as id, usr.last_name as name, usr.login as login, usr.signature as sign " +
                        "FROM bpm_user usr " +
                        "WHERE usr.company_id = ? AND usr.deleted = 'false' " +
                        "ORDER BY name ";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(getAllUsersSql);
             ) {
            pstmt.setLong(1, id);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                List<UserEntity> userEntityList = new ArrayList<>();
                while (resultSet.next()) {
                    UserEntity user = UserEntity.builder()
                            .id(resultSet.getLong("id"))
                            .lastName(resultSet.getString("name"))
                            .login(resultSet.getString("login"))
                            .signature(resultSet.getBytes("sign"))
                            .build();
                    userEntityList.add(user);
                }
                return userEntityList;
            }
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Database connection error.");
        }
    }
}
