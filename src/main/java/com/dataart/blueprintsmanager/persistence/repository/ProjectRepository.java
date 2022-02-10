package com.dataart.blueprintsmanager.persistence.repository;

import com.dataart.blueprintsmanager.exceptions.DataBaseCustomApplicationException;
import com.dataart.blueprintsmanager.persistence.entity.CompanyEntity;
import com.dataart.blueprintsmanager.persistence.entity.ProjectEntity;
import com.dataart.blueprintsmanager.persistence.entity.StageEntity;
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
public class ProjectRepository {
    private final DataSource dataSource;

    public ProjectRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<ProjectEntity> findAll() {
        String getAllProjectsSql =
                "SELECT prj.id as id, prj.name as name, prj.code as code, prj.edit_time as time, prj.reassembly_required as reassembly, stg.code as stage, stg.name stgName, cmp.name as cmpname " +
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
                        .stage(StageEntity.builder().code(resultSet.getString("stage")).name(resultSet.getString("stgName")).build())
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

    public ProjectEntity fetchById(Long projectId) {
        String getProjectByIdSql =
                "SELECT  id, name, object_name as objName, object_address as objAddr, release_date as relDate, " +
                        "volume_number as volNumber, subname, code, designer_id as designerId, " +
                        "supervisor_id as supervisorId, chief_id as chiefId, controller_id as controllerId, " +
                        "company_id as companyId, stage_id as stageId, reassembly_required as reassembly, edit_time as eTime " +
                        "FROM bpm_project as prj " +
                        "WHERE deleted = 'false' AND  id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(getProjectByIdSql)) {
            pstmt.setLong(1, projectId);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    return ProjectEntity.builder()
                            .id(resultSet.getLong("id"))
                            .name(resultSet.getString("name"))
                            .objectName(resultSet.getString("objName"))
                            .objectAddress(resultSet.getString("objAddr"))
                            .releaseDate(resultSet.getDate("relDate").toLocalDate())
                            .volumeNumber(resultSet.getLong("volNumber"))
                            .volumeName(resultSet.getString("subname"))
                            .code(resultSet.getString("code"))
                            .designer(UserEntity.builder().id(resultSet.getLong("designerId")).build())
                            .supervisor(UserEntity.builder().id(resultSet.getLong("supervisorId")).build())
                            .chief(UserEntity.builder().id(resultSet.getLong("chiefId")).build())
                            .controller(UserEntity.builder().id(resultSet.getLong("controllerId")).build())
                            .company(CompanyEntity.builder().id(resultSet.getLong("companyId")).build())
                            .stage(StageEntity.builder().id(resultSet.getLong("stageId")).build())
                            .reassemblyRequired(resultSet.getBoolean("reassembly"))
                            .editTime(resultSet.getTimestamp("eTime").toLocalDateTime())
                            .build();
                }
                String message = String.format("Project with id= %d not found", projectId);
                log.debug(message);
                throw new DataBaseCustomApplicationException(message);
            }
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Database connection error.");
        }
    }
}
