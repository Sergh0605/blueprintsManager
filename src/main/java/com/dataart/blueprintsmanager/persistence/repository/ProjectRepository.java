package com.dataart.blueprintsmanager.persistence.repository;

import com.dataart.blueprintsmanager.exceptions.DataBaseCustomApplicationException;
import com.dataart.blueprintsmanager.persistence.entity.CompanyEntity;
import com.dataart.blueprintsmanager.persistence.entity.ProjectEntity;
import com.dataart.blueprintsmanager.persistence.entity.StageEntity;
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
public class ProjectRepository {
    private final DataSource dataSource;
    private final DocumentRepository documentRepository;

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

    public ProjectEntity fetchByIdTransactional(Long projectId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            ProjectEntity projectEntity = fetchById(projectId, connection);
            connection.commit();
            log.debug(String.format("Project with id = %d founded", projectId));
            return projectEntity;
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Database connection error.");
        }
    }

    private ProjectEntity fetchById(Long projectId, Connection connection) throws SQLException {
        log.debug(String.format("Try to find Project with id = %d", projectId));
        String getProjectByIdSql =
                "SELECT  id, name, object_name as objName, object_address as objAddr, release_date as relDate, " +
                        "volume_number as volNumber, subname, code, designer_id as designerId, " +
                        "supervisor_id as supervisorId, chief_id as chiefId, controller_id as controllerId, " +
                        "company_id as companyId, stage_id as stageId, reassembly_required as reassembly, edit_time as eTime " +
                        "FROM bpm_project as prj " +
                        "WHERE deleted = 'false' AND  id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(getProjectByIdSql)) {
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
        }
    }

    public ProjectEntity updateTransactional(ProjectEntity project) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                log.debug(String.format("Try to update project with id = %d", project.getId()));
                update(project, connection);
                int documentsAffected = documentRepository.setReassemblyRequiredByProjectId(project.getId(), connection);
                ProjectEntity projectEntity = fetchById(project.getId(), connection);
                connection.commit();
                log.debug(String.format("Reassembly_required set true for %d documents in Project with id = %d", documentsAffected, project.getId()));
                log.debug(String.format("Project with id = %d is updated", project.getId()));
                return projectEntity;
            } catch (SQLException e) {
                connection.rollback();
                log.debug(e.getMessage());
                throw new DataBaseCustomApplicationException("Database unexpected error.");
            }
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Database connection error.");
        }
    }

    private void update(ProjectEntity project, Connection connection) throws SQLException {
        String updateProjectByIdSql =
                "UPDATE  bpm_project SET name = ?, object_name = ?, object_address = ?, release_date = ?, " +
                        "volume_number = ?, subname = ?, code = ?, designer_id = ?, " +
                        "supervisor_id = ?, chief_id = ?, controller_id = ?, " +
                        "company_id = ?, stage_id = ?, reassembly_required = ?, edit_time = ? " +
                        "WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(updateProjectByIdSql)) {
            pstmt.setString(1, project.getName());
            pstmt.setString(2, project.getObjectName());
            pstmt.setString(3, project.getObjectAddress());
            pstmt.setDate(4, Date.valueOf(project.getReleaseDate()));
            pstmt.setLong(5, project.getVolumeNumber());
            pstmt.setString(6, project.getVolumeName());
            pstmt.setString(7, project.getCode());
            pstmt.setLong(8, project.getDesigner().getId());
            pstmt.setLong(9, project.getSupervisor().getId());
            pstmt.setLong(10, project.getChief().getId());
            pstmt.setLong(11, project.getChief().getId());
            pstmt.setLong(12, project.getCompany().getId());
            pstmt.setLong(13, project.getStage().getId());
            pstmt.setBoolean(14, project.getReassemblyRequired());
            pstmt.setTimestamp(15, Timestamp.valueOf(project.getEditTime()));
            pstmt.setLong(16, project.getId());
            pstmt.executeUpdate();
        }
    }

    public ProjectEntity createTransactional(ProjectEntity project) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                log.debug("Try to create new project");
                Long createdProjectId = create(project, connection);
                ProjectEntity projectEntity = fetchById(createdProjectId, connection);
                connection.commit();
                log.debug(String.format("Project with id = %d is created", projectEntity.getId()));
                return projectEntity;
            } catch (SQLException e) {
                connection.rollback();
                log.debug(e.getMessage());
                throw new DataBaseCustomApplicationException("Database unexpected error.");
            }
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Database connection error.");
        }
    }

    private Long create(ProjectEntity project, Connection connection) throws SQLException {
        String createProjectSql =
                "INSERT INTO bpm_project ( " +
                        "name, object_name, object_address, release_date, volume_number, subname, code, designer_id, " +
                        "supervisor_id, chief_id, controller_id, company_id, stage_id, reassembly_required, edit_time) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(createProjectSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, project.getName());
            pstmt.setString(2, project.getObjectName());
            pstmt.setString(3, project.getObjectAddress());
            pstmt.setDate(4, Date.valueOf(project.getReleaseDate()));
            pstmt.setLong(5, project.getVolumeNumber());
            pstmt.setString(6, project.getVolumeName());
            pstmt.setString(7, project.getCode());
            pstmt.setLong(8, project.getDesigner().getId());
            pstmt.setLong(9, project.getSupervisor().getId());
            pstmt.setLong(10, project.getChief().getId());
            pstmt.setLong(11, project.getChief().getId());
            pstmt.setLong(12, project.getCompany().getId());
            pstmt.setLong(13, project.getStage().getId());
            pstmt.setBoolean(14, project.getReassemblyRequired());
            pstmt.setTimestamp(15, Timestamp.valueOf(project.getEditTime()));
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating project failed, no rows affected.");
            }
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else {
                    throw new SQLException("Creating project failed, no ID obtained.");
                }
            }
        }
    }
}
