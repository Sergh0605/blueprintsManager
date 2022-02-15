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
import java.util.Optional;

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
                    .designer(getUser((Long) resultSet.getObject("designerId"), connection))
                    .supervisor(getUser((Long) resultSet.getObject("supervisorId"), connection))
                    .chief(getUser((Long) resultSet.getObject("chiefId"), connection))
                    .controller(getUser((Long) resultSet.getObject("controllerId"), connection))
                    .company(getCompany((Long) resultSet.getObject("companyId"), connection))
                    .stage(getStage((Long) resultSet.getObject("stageId"), connection))
                    .reassemblyRequired(resultSet.getBoolean("reassembly"))
                    .editTime(resultSet.getTimestamp("editTime").toLocalDateTime())
                    .build();
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Can't parse Project object from DB");
        }
    }

    private UserEntity getUser(Long userId, Connection connection) {
        return Optional.ofNullable(userId).map(x -> userRepository.fetchById(x, connection, true)).orElse(null);
    }

    private CompanyEntity getCompany(Long companyId, Connection connection) {
        return Optional.ofNullable(companyId).map(x -> companyRepository.fetchById(x, connection)).orElse(null);
    }

    private StageEntity getStage(Long stageId, Connection connection) {
        return Optional.ofNullable(stageId).map(x -> stageRepository.fetchById(x, connection)).orElse(null);
    }

    public List<ProjectEntity> fetchAllTransactional() {
        String getAllProjectsSql =
                "SELECT id, name, object_name as objName, object_address as objAddr, release_date as date, " +
                        "volume_number as volNumber, subname, code, designer_id as designerId, " +
                        "supervisor_id as supervisorId, chief_id as chiefId, controller_id as controllerId, " +
                        "company_id as companyId, stage_id as stageId, reassembly_required as reassembly, edit_time as editTime " +
                        "FROM bpm_project " +
                        "WHERE deleted = false " +
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
                        "WHERE deleted = false AND  id = ?";
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

    protected int setReassemblyRequiredById(Long projectId, Connection connection) throws SQLException {
        String setReassemblyByIdSql =
                "UPDATE  bpm_project SET reassembly_required = true " +
                        "WHERE id = ? AND deleted = false";
        try (PreparedStatement pstmt = connection.prepareStatement(setReassemblyByIdSql)) {
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
            pstmt.setObject(8, Optional.ofNullable(project.getDesigner()).map(UserEntity::getId).orElse(null));
            pstmt.setObject(9, Optional.ofNullable(project.getSupervisor()).map(UserEntity::getId).orElse(null));
            pstmt.setObject(10, Optional.ofNullable(project.getChief()).map(UserEntity::getId).orElse(null));
            pstmt.setObject(11, Optional.ofNullable(project.getController()).map(UserEntity::getId).orElse(null));
            pstmt.setObject(12, Optional.ofNullable(project.getCompany()).map(CompanyEntity::getId).orElse(null));
            pstmt.setObject(13, Optional.ofNullable(project.getStage()).map(StageEntity::getId).orElse(null));
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

    public ProjectEntity updateProjectInPdfTransactional(Long projectId, byte[] projectInPdf) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                log.debug(String.format("Try to update Project file in PDF with id = %d", projectId));
                updateProjectInPdf(projectId, projectInPdf, connection);
                ProjectEntity projectEntity = fetchById(projectId, connection);
                connection.commit();
                log.debug(String.format("Project in PDF updated in Document with id = %d", projectId));
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

    private void updateProjectInPdf(Long projectId, byte[] projectInPdf, Connection connection) throws SQLException {
        String updateFileInDocumentByIdSql =
                "UPDATE  bpm_project SET project_in_pdf = ?, reassembly_required = false " +
                        "WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(updateFileInDocumentByIdSql)) {
            pstmt.setBytes(1, projectInPdf);
            pstmt.setLong(2, projectId);
            pstmt.executeUpdate();
        }
    }

    public byte[] fetchProjectInPdfByProjectIdTransactional(Long projectId) {
        try (Connection connection = dataSource.getConnection()) {
            log.debug(String.format("Try to find Project in PDF for Document with id = %d", projectId));
            String getContentInPdfSql =
                    "SELECT project_in_pdf " +
                            "FROM bpm_project " +
                            "WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(getContentInPdfSql)) {
                pstmt.setLong(1, projectId);
                try (ResultSet resultSet = pstmt.executeQuery()) {
                    if (resultSet.next()) {
                        byte[] projectInPdf = resultSet.getBytes(1);
                        if (projectInPdf != null && projectInPdf.length > 0) {
                            log.debug(String.format("Project in Pdf for Project with id = %d found", projectId));
                            return projectInPdf;
                        }
                        String message = String.format("Project in Pdf with id = %d not found", projectId);
                        log.debug(message);
                        throw new DataBaseCustomApplicationException(message);
                    }
                    String message = String.format("Project with id= %d not found", projectId);
                    log.debug(message);
                    throw new DataBaseCustomApplicationException(message);
                }
            }
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new DataBaseCustomApplicationException("Database connection error.");
        }
    }

    protected int deleteById(Long projectId, Connection connection) {
        log.debug(String.format("Try to set deleted Project with id = %d", projectId));
        String setDeleteByProjectIdSql =
                "UPDATE  bpm_project SET deleted = true, reassembly_required = false " +
                        "WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(setDeleteByProjectIdSql)) {
            pstmt.setLong(1, projectId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
