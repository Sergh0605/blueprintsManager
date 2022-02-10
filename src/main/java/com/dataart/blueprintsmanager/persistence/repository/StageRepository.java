package com.dataart.blueprintsmanager.persistence.repository;

import com.dataart.blueprintsmanager.exceptions.DataBaseCustomApplicationException;
import com.dataart.blueprintsmanager.persistence.entity.StageEntity;
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
public class StageRepository {
    private final DataSource dataSource;

    public List<StageEntity> findAll() {
        String getAllStagesSql =
                "SELECT * " +
                        "FROM bpm_stage " +
                        "ORDER BY name ";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(getAllStagesSql);
             ResultSet resultSet = pstmt.executeQuery()) {
            List<StageEntity> stageEntityList = new ArrayList<>();
            while (resultSet.next()) {
                StageEntity stage = StageEntity.builder()
                        .id(resultSet.getLong("id"))
                        .name(resultSet.getString("name"))
                        .code(resultSet.getString("code"))
                        .build();
                stageEntityList.add(stage);
            }
            return stageEntityList;
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Database connection error.");
        }
    }
}
