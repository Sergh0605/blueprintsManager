package com.dataart.blueprintsmanager.persistence.repository;

import com.dataart.blueprintsmanager.exceptions.DataBaseCustomApplicationException;
import com.dataart.blueprintsmanager.persistence.entity.CompanyEntity;
import com.dataart.blueprintsmanager.persistence.entity.ProjectEntity;
import com.dataart.blueprintsmanager.persistence.entity.StageEntity;
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
public class ProjectRepository {
    private final DataSource dataSource;

    public ProjectRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<ProjectEntity> findAll() {
        String getAllProjectsSql =
                        "SELECT prj.id as id, prj.name as name, prj.code as code, prj.edit_time as time, prj.reassembly_required as reassembly, stg.code as stage, cmp.name as cmpname " +
                        "FROM bpm_project prj " +
                        "INNER JOIN bpm_company cmp ON prj.company_id = cmp.id " +
                        "INNER JOIN bpm_stage stg ON prj.stage_id = stg.id " +
                        "WHERE prj.deleted = 'false' " +
                        "ORDER BY time DESC";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(getAllProjectsSql);
             ResultSet resultSet = pstmt.executeQuery()) {
            List<ProjectEntity> projectEntityList = new ArrayList<>();
            while (resultSet.next()) {
                ProjectEntity project = ProjectEntity.builder()
                        .id(resultSet.getLong("id"))
                        .name(resultSet.getString("name"))
                        .code(resultSet.getString("code"))
                        .editTime(resultSet.getTimestamp("time").toLocalDateTime())
                        .reassemblyRequired(resultSet.getBoolean("reassembly"))
                        .stage(StageEntity.builder().code(resultSet.getString("stage")).build())
                        .company(CompanyEntity.builder().name(resultSet.getString("cmpname")).build())
                        .build();
                projectEntityList.add(project);
            }
            return projectEntityList;
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Database connection error.");
        }
    }
}
