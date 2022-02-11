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

    private StageEntity buildStage(ResultSet resultSet) {
        try {
            return StageEntity.builder()
                    .id(resultSet.getLong("id"))
                    .name(resultSet.getString("name"))
                    .code(resultSet.getString("code"))
                    .build();
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Can't parse Stage object from DB");
        }
    }

    public List<StageEntity> fetchAllTransactional() {
        String getAllStagesSql =
                "SELECT * " +
                        "FROM bpm_stage " +
                        "ORDER BY name ";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(getAllStagesSql);
             ResultSet resultSet = pstmt.executeQuery()) {
            List<StageEntity> stageEntityList = new ArrayList<>();
            Integer stageCount = 0;
            while (resultSet.next()) {
                StageEntity stage = buildStage(resultSet);
                stageEntityList.add(stage);
                stageCount++;
            }
            log.debug(String.format("%d Stages found", stageCount));
            return stageEntityList;
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Database connection error.");
        }
    }

    protected StageEntity fetchById(Long stageId, Connection connection) {
        String getStageByIdSql =
                "SELECT *" +
                        "FROM bpm_stage " +
                        "WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(getStageByIdSql)) {
            pstmt.setLong(1, stageId);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    log.debug(String.format("Stage with id= %d found", stageId));
                    return buildStage(resultSet);
                }
                String message = String.format("Stage with id= %d not found", stageId);
                log.debug(message);
                throw new DataBaseCustomApplicationException(message);
            }
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Database connection error.");
        }
    }
}
