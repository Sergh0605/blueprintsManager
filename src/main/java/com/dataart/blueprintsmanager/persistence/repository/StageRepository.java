package com.dataart.blueprintsmanager.persistence.repository;

import com.dataart.blueprintsmanager.exceptions.CustomApplicationException;
import com.dataart.blueprintsmanager.exceptions.DataBaseCustomApplicationException;
import com.dataart.blueprintsmanager.exceptions.NotFoundCustomApplicationException;
import com.dataart.blueprintsmanager.persistence.entity.StageEntity;
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
public class StageRepository {
    private final DataSource dataSource;

    public StageEntity fetchById(Long stageId) {
        try (Connection connection = dataSource.getConnection()) {
                return fetchById(stageId, connection);
            } catch (SQLException e) {
                log.error(e.getMessage());
                throw new DataBaseCustomApplicationException("Database unexpected error.", e);
            } catch (CustomApplicationException e) {
                log.info(e.getMessage());
                throw e;
            }
    }

    public List<StageEntity> fetchAll() {
        log.info("Try to find All Stages");
        String getAllStagesSql =
                "SELECT * " +
                        "FROM bpm_stage " +
                        "ORDER BY name ";
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet resultSet = stmt.executeQuery(getAllStagesSql)) {
            List<StageEntity> stageEntityList = new ArrayList<>();
            while (resultSet.next()) {
                StageEntity stage = buildStage(resultSet);
                stageEntityList.add(stage);
            }
            log.debug(String.format("%d Stages found", stageEntityList.size()));
            return stageEntityList;
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw new DataBaseCustomApplicationException("Database connection error.");
        }
    }

    protected StageEntity fetchById(Long stageId, Connection connection) throws SQLException {
        log.info(String.format("Try to find Stage with id = %d", stageId));
        String getStageByIdSql =
                "SELECT *" +
                        "FROM bpm_stage " +
                        "WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(getStageByIdSql)) {
            pstmt.setLong(1, stageId);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    log.info(String.format("Stage with id= %d found", stageId));
                    return buildStage(resultSet);
                }
                throw new NotFoundCustomApplicationException(String.format("Stage with id= %d not found", stageId));
            }
        }
    }

    private StageEntity buildStage(ResultSet resultSet) throws SQLException {
        return StageEntity.builder()
                .id(resultSet.getLong("id"))
                .name(resultSet.getString("name"))
                .code(resultSet.getString("code"))
                .build();
    }
}
