package com.dataart.blueprintsmanager.persistence.repository;

import com.dataart.blueprintsmanager.exceptions.DataBaseCustomApplicationException;
import com.dataart.blueprintsmanager.persistence.entity.ProjectEntity;
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
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final StageRepository stageRepository;

    private ProjectEntity buildProject(ResultSet resultSet, Connection connection) {
        try {
            return ProjectEntity.builder()
                    .id(resultSet.getLong("id"))
                    .name(resultSet.getString("name"))
                    .objectName(resultSet.getString("objName"))
                    .objectAddress(resultSet.getString("objAddr"))
                    .releaseDate(resultSet.getDate("date").toLocalDate())
                    .volumeNumber(resultSet.getLong("volNumber"))
                    .volumeName(resultSet.getString("subname"))
                    .code(resultSet.getString("code"))
                    .designer(userRepository.fetchById(resultSet.getLong("designerId"), connection))
                    .supervisor(userRepository.fetchById(resultSet.getLong("supervisorId"), connection))
                    .chief(userRepository.fetchById(resultSet.getLong("chiefId"), connection))
                    .controller(userRepository.fetchById(resultSet.getLong("controllerId"), connection))
                    .company(companyRepository.fetchById(resultSet.getLong("companyId"), connection))
                    .stage(stageRepository.fetchById(resultSet.getLong("stageId"), connection))
                    .reassemblyRequired(resultSet.getBoolean("reassembly"))
                    .editTime(resultSet.getTimestamp("editTime").toLocalDateTime())
                    .build();
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Can't parse Project object from DB");
        }
    }

    public List<ProjectEntity> fetchAllTransactional() {
        String getAllProjectsSql =
                "SELECT id, name, object_name as objName, object_address as objAddr, release_date as date, " +
                        "volume_number as volNumber, subname, code, designer_id as designerId, " +
                        "supervisor_id as supervisorId, chief_id as chiefId, controller_id as controllerId, " +
                        "company_id as companyId, stage_id as stageId, reassembly_required as reassembly, edit_time as editTime " +
                        "FROM bpm_project " +
                        "WHERE deleted = 'false' " +
                        "ORDER BY editTime DESC";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(getAllProjectsSql);
             ResultSet resultSet = pstmt.executeQuery()) {
            List<ProjectEntity> projectEntityList = new ArrayList<>();
            Integer projectCount = 0;
            while (resultSet.next()) {
                ProjectEntity project = buildProject(resultSet, connection);
                projectEntityList.add(project);
                projectCount++;
            }
            log.debug(String.format("%d Project found", projectCount));
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
            return projectEntity;
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Database connection error.");
        }
    }

    protected ProjectEntity fetchById(Long projectId, Connection connection) throws SQLException {
        log.debug(String.format("Try to find Project with id = %d", projectId));
        String getProjectByIdSql =
                "SELECT id, name, object_name as objName, object_address as objAddr, release_date as date, " +
                        "volume_number as volNumber, subname, code, designer_id as designerId, " +
                        "supervisor_id as supervisorId, chief_id as chiefId, controller_id as controllerId, " +
                        "company_id as companyId, stage_id as stageId, reassembly_required as reassembly, edit_time as editTime " +
                        "FROM bpm_project " +
                        "WHERE deleted = 'false' AND  id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(getProjectByIdSql)) {
            pstmt.setLong(1, projectId);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    log.debug(String.format("Project with id = %d found", projectId));
                    return buildProject(resultSet, connection);
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
                int documentsAffected = setReassemblyRequiredByProjectId(project.getId(), connection);
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

    private int setReassemblyRequiredByProjectId(Long projectId, Connection connection) throws SQLException {
        String updateProjectByIdSql =
                "UPDATE  bpm_document SET reassembly_required = true " +
                        "WHERE project_id = ? AND deleted = false";
        try (PreparedStatement pstmt = connection.prepareStatement(updateProjectByIdSql)) {
            pstmt.setLong(1, projectId);
            return pstmt.executeUpdate();
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
