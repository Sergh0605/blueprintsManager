package com.dataart.blueprintsmanager.persistence.repository;

import com.dataart.blueprintsmanager.exceptions.CustomApplicationException;
import com.dataart.blueprintsmanager.exceptions.DataBaseCustomApplicationException;
import com.dataart.blueprintsmanager.exceptions.NotFoundCustomApplicationException;
import com.dataart.blueprintsmanager.persistence.entity.CompanyEntity;
import com.dataart.blueprintsmanager.persistence.entity.ProjectEntity;
import com.dataart.blueprintsmanager.persistence.entity.StageEntity;
import com.dataart.blueprintsmanager.persistence.entity.UserEntity;
import com.dataart.blueprintsmanager.util.CustomPage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    public void deleteProjectTransactional(Long projectId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                log.info(String.format("Try to transactional delete project with id = %d", projectId));
                deleteDocumentsByProjectId(projectId, connection);
                deleteById(projectId, connection);
                connection.commit();
                log.info(String.format("Project with id = %d transactional deleted", projectId));
            } catch (SQLException e) {
                connection.rollback();
                log.error(e.getMessage(), e);
                throw new DataBaseCustomApplicationException("Database unexpected error.", e);
            } catch (CustomApplicationException e) {
                connection.rollback();
                log.info(e.getMessage());
                throw e;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataBaseCustomApplicationException("Database connection error.", e);
        }
    }

    public List<ProjectEntity> fetchAll() {
        String getAllProjectsSql =
                "SELECT id, name, object_name as objName, object_address as objAddr, release_date as date, " +
                        "volume_number as volNumber, subname, code, designer_id as designerId, " +
                        "supervisor_id as supervisorId, chief_id as chiefId, controller_id as controllerId, " +
                        "company_id as companyId, stage_id as stageId, reassembly_required as reassembly, edit_time as editTime " +
                        "FROM bpm_project " +
                        "WHERE deleted = false " +
                        "ORDER BY editTime DESC";
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet resultSet = stmt.executeQuery(getAllProjectsSql)) {
            List<ProjectEntity> projectEntityList = new ArrayList<>();
            while (resultSet.next()) {
                ProjectEntity project = buildProject(resultSet, connection);
                projectEntityList.add(project);
            }
            log.info(String.format("%d Project found", projectEntityList.size()));
            return projectEntityList;
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw new DataBaseCustomApplicationException("Database connection error.");
        }
    }

    public ProjectEntity fetchByIdWrapped(Long projectId) {
        try (Connection connection = dataSource.getConnection()) {
            return fetchById(projectId, connection);
        } catch (CustomApplicationException e) {
            log.info(e.getMessage());
            throw e;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataBaseCustomApplicationException("Database connection error.", e);
        }
    }

    public ProjectEntity updateTransactional(ProjectEntity project) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                log.info(String.format("Try to transactional update project with id = %d", project.getId()));
                update(project, connection);
                setDocumentsReassemblyRequiredById(project.getId(), connection);
                ProjectEntity projectEntity = fetchById(project.getId(), connection);
                connection.commit();
                log.info(String.format("Project with id = %d is updated", project.getId()));
                return projectEntity;
            } catch (SQLException e) {
                connection.rollback();
                log.error(e.getMessage(), e);
                throw new DataBaseCustomApplicationException("Database unexpected error.", e);
            } catch (CustomApplicationException e) {
                connection.rollback();
                log.info(e.getMessage());
                throw e;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataBaseCustomApplicationException("Database connection error.", e);
        }
    }

    public ProjectEntity createTransactional(ProjectEntity project) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                log.info("Try to Transactional create new project");
                Long createdProjectId = create(project, connection);
                ProjectEntity projectEntity = fetchById(createdProjectId, connection);
                connection.commit();
                log.info(String.format("Project with id = %d transactional created", projectEntity.getId()));
                return projectEntity;
            } catch (SQLException e) {
                connection.rollback();
                log.error(e.getMessage(), e);
                throw new DataBaseCustomApplicationException("Database unexpected error.", e);
            } catch (CustomApplicationException e) {
                connection.rollback();
                log.info(e.getMessage());
                throw e;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataBaseCustomApplicationException("Database connection error.", e);
        }
    }

    public ProjectEntity updateProjectInPdfTransactional(Long projectId, byte[] projectInPdf) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                log.info(String.format("Try to Transactional update Project file in PDF with id = %d", projectId));
                updateProjectInPdf(projectId, projectInPdf, connection);
                ProjectEntity projectEntity = fetchById(projectId, connection);
                connection.commit();
                log.info(String.format("Project in PDF Transactional updated in Document with id = %d", projectId));
                return projectEntity;
            } catch (SQLException e) {
                connection.rollback();
                log.error(e.getMessage(), e);
                throw new DataBaseCustomApplicationException("Database unexpected error.", e);
            } catch (CustomApplicationException e) {
                connection.rollback();
                log.info(e.getMessage());
                throw e;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataBaseCustomApplicationException("Database connection error.", e);
        }
    }

    public byte[] fetchProjectInPdfByProjectId(Long projectId) {
        log.info(String.format("Try to find Project in PDF for Document with id = %d", projectId));
        String getContentInPdfSql =
                "SELECT project_in_pdf " +
                        "FROM bpm_project " +
                        "WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(getContentInPdfSql)) {
            pstmt.setLong(1, projectId);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    byte[] projectInPdf = resultSet.getBytes(1);
                    if (projectInPdf != null && projectInPdf.length > 0) {
                        log.debug(String.format("Project in Pdf for Project with id = %d found", projectId));
                        return projectInPdf;
                    }
                }
                String message = String.format("Project with id= %d not found", projectId);
                log.info(message);
                throw new NotFoundCustomApplicationException(message);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataBaseCustomApplicationException("Database connection error.", e);
        }
    }

    protected ProjectEntity fetchById(Long projectId, Connection connection) throws SQLException {
        log.info(String.format("Try to find Project with id = %d", projectId));
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
                    log.info(String.format("Project with id = %d found", projectId));
                    return buildProject(resultSet, connection);
                }
                throw new NotFoundCustomApplicationException(String.format("Project with id= %d not found", projectId));
            }
        }
    }

    private void deleteDocumentsByProjectId(Long projectId, Connection connection) throws SQLException {
        log.info(String.format("Try to delete All Documents in Project with id = %d", projectId));
        String setDeleteByProjectIdSql =
                "UPDATE  bpm_document SET deleted = true, reassembly_required = false " +
                        "WHERE project_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(setDeleteByProjectIdSql)) {
            pstmt.setLong(1, projectId);
            int countOfAffectedRows = pstmt.executeUpdate();
            if (countOfAffectedRows > 0) {
                log.info(String.format("All Documents in Project with id = %d deleted", projectId));
            } else {
                log.info(String.format("Project with id = %d not found or project hasn't any Documents", projectId));
            }
        }
    }

    protected void setReassemblyRequiredById(Long projectId, Connection connection) throws SQLException {
        log.info(String.format("Try to set Reassembly required Project with id = %d", projectId));
        String setReassemblyByIdSql =
                "UPDATE  bpm_project SET reassembly_required = true " +
                        "WHERE id = ? AND deleted = false";
        try (PreparedStatement pstmt = connection.prepareStatement(setReassemblyByIdSql)) {
            pstmt.setLong(1, projectId);
            int countOfAffectedRows = pstmt.executeUpdate();
            if (countOfAffectedRows > 0) {
                log.info(String.format("Project with id = %d set to Reassembly required", projectId));
            } else {
                throw new NotFoundCustomApplicationException(String.format("Can't set Reassembly required. Project with id = %d not found", projectId));
            }
        }
    }

    protected void setDocumentsReassemblyRequiredById(Long projectId, Connection connection) throws SQLException {
        log.info(String.format("Try to set Reassembly required for All Documents of Project with id = %d", projectId));
        String setReassemblyByIdSql =
                "UPDATE bpm_document SET reassembly_required = true " +
                        "WHERE project_id = ? AND deleted = false";
        try (PreparedStatement pstmt = connection.prepareStatement(setReassemblyByIdSql)) {
            pstmt.setLong(1, projectId);
            int countOfAffectedRows = pstmt.executeUpdate();
            if (countOfAffectedRows > 0) {
                log.info(String.format("All Documents in Project with id = %d set to Reassembly required", projectId));
            } else {
                log.info(String.format("Can't set Reassembly required. There is no Documents in Project with id = %d", projectId));
            }
        }
    }

    private void update(ProjectEntity project, Connection connection) throws SQLException {
        log.info(String.format("Try to update Project with id = %d", project.getId()));
        String updateProjectByIdSql =
                "UPDATE  bpm_project SET name = ?, object_name = ?, object_address = ?, release_date = ?, " +
                        "volume_number = ?, subname = ?, code = ?, designer_id = ?, " +
                        "supervisor_id = ?, chief_id = ?, controller_id = ?, " +
                        "stage_id = ?, reassembly_required = ?, edit_time = ? " +
                        "WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(updateProjectByIdSql)) {
            pstmt.setString(1, project.getName());
            pstmt.setString(2, project.getObjectName());
            pstmt.setString(3, project.getObjectAddress());
            pstmt.setObject(4, project.getReleaseDate());
            pstmt.setLong(5, project.getVolumeNumber());
            pstmt.setString(6, project.getVolumeName());
            pstmt.setString(7, project.getCode());
            pstmt.setObject(8, Optional.ofNullable(project.getDesigner()).map(UserEntity::getId).orElse(null));
            pstmt.setObject(9, Optional.ofNullable(project.getSupervisor()).map(UserEntity::getId).orElse(null));
            pstmt.setObject(10, Optional.ofNullable(project.getChief()).map(UserEntity::getId).orElse(null));
            pstmt.setObject(11, Optional.ofNullable(project.getController()).map(UserEntity::getId).orElse(null));
            pstmt.setObject(12, Optional.ofNullable(project.getStage()).map(StageEntity::getId).orElse(null));
            pstmt.setBoolean(13, project.getReassemblyRequired());
            pstmt.setTimestamp(14, Timestamp.valueOf(project.getEditTime()));
            pstmt.setLong(15, project.getId());
            int countOfAffectedRows = pstmt.executeUpdate();
            if (countOfAffectedRows > 0) {
                log.info(String.format("Project with id = %d updated", project.getId()));
            } else {
                throw new NotFoundCustomApplicationException(String.format("Project with id = %d not found", project.getId()));
            }
        }
    }

    private Long create(ProjectEntity project, Connection connection) throws SQLException {
        log.info("Try to create Project");
        String createProjectSql =
                "INSERT INTO bpm_project ( " +
                        "name, object_name, object_address, release_date, volume_number, subname, code, designer_id, " +
                        "supervisor_id, chief_id, controller_id, company_id, stage_id, reassembly_required, edit_time) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(createProjectSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, project.getName());
            pstmt.setString(2, project.getObjectName());
            pstmt.setString(3, project.getObjectAddress());
            pstmt.setObject(4, project.getReleaseDate());
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
            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long key = generatedKeys.getLong(1);
                    log.info(String.format("New Project with id = %d created", key));
                    return key;
                } else {
                    throw new DataBaseCustomApplicationException("Creating project failed, no ID obtained.");
                }
            }
        }
    }

    private void updateProjectInPdf(Long projectId, byte[] projectInPdf, Connection connection) throws SQLException {
        String updateFileInDocumentByIdSql =
                "UPDATE  bpm_project SET project_in_pdf = ?, reassembly_required = false " +
                        "WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(updateFileInDocumentByIdSql)) {
            pstmt.setBytes(1, projectInPdf);
            pstmt.setLong(2, projectId);
            int countOfAffectedRows = pstmt.executeUpdate();
            if (countOfAffectedRows > 0) {
                log.info(String.format("Project in Pdf in Project with id = %d updated", projectId));
            } else {
                throw new NotFoundCustomApplicationException(String.format("Can't update. Project with id = %d not found", projectId));
            }
        }
    }

    protected void deleteById(Long projectId, Connection connection) throws SQLException {
        log.info(String.format("Try to set deleted Project with id = %d", projectId));
        String setDeleteByProjectIdSql =
                "UPDATE  bpm_project SET deleted = true, reassembly_required = false " +
                        "WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(setDeleteByProjectIdSql)) {
            pstmt.setLong(1, projectId);
            int countOfAffectedRows = pstmt.executeUpdate();
            if (countOfAffectedRows > 0) {
                log.info(String.format("Project with id = %d deleted", projectId));
            } else {
                throw new NotFoundCustomApplicationException(String.format("Can't delete. Project with id = %d not found", projectId));
            }
        }
    }

    private ProjectEntity buildProject(ResultSet resultSet, Connection connection) throws SQLException {
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
    }

    private UserEntity getUser(Long userId, Connection connection) {
        return Optional.ofNullable(userId).map(x -> {
            try {
                return userRepository.fetchById(x, connection, true);
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new DataBaseCustomApplicationException("Database Unexpected error", e);
            }
        }).orElse(null);
    }

    private CompanyEntity getCompany(Long companyId, Connection connection) {
        return Optional.ofNullable(companyId).map(x -> {
            try {
                return companyRepository.fetchById(x, connection);
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new DataBaseCustomApplicationException("Database Unexpected error", e);
            }
        }).orElse(null);
    }

    private StageEntity getStage(Long stageId, Connection connection) {
        return Optional.ofNullable(stageId).map(x -> {
            try {
                return stageRepository.fetchById(x, connection);
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new DataBaseCustomApplicationException("Database unexpected error", e);
            }
        }).orElse(null);
    }

    public CustomPage<ProjectEntity> fetchAllPaginated(Pageable pageable) {
        log.info("Try to find {} page with {} Projects", pageable.getPageNumber(), pageable.getPageSize());
        int countForLimit = pageable.getPageSize();
        int countForOffset = (pageable.getPageNumber() - 1) * pageable.getPageSize();
        String getAllProjectsSql =
                "SELECT id, name, object_name as objName, object_address as objAddr, release_date as date, " +
                        "volume_number as volNumber, subname, code, designer_id as designerId, " +
                        "supervisor_id as supervisorId, chief_id as chiefId, controller_id as controllerId, " +
                        "company_id as companyId, stage_id as stageId, reassembly_required as reassembly, edit_time as editTime " +
                        "FROM bpm_project " +
                        "WHERE deleted = false " +
                        "ORDER BY editTime DESC " +
                        "LIMIT ? " +
                        "OFFSET ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(getAllProjectsSql)) {
            pstmt.setInt(1, countForLimit);
            pstmt.setInt(2, countForOffset);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                List<ProjectEntity> projectEntityList = new ArrayList<>();
                while (resultSet.next()) {
                    ProjectEntity project = buildProject(resultSet, connection);
                    projectEntityList.add(project);
                }
                log.info(String.format("%d Projects found on page %d", projectEntityList.size(), pageable.getPageNumber()));
                Integer countOfProjects = fetchCountOfProjects(connection);
                return new CustomPage<>(projectEntityList, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()), countOfProjects);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataBaseCustomApplicationException("Database connection error.", e);
        }
    }

    private Integer fetchCountOfProjects(Connection connection) throws SQLException {
        log.info("Try to find Projects count");
        String getProjectCountSql =
                "SELECT COUNT(*) as projectCount " +
                        "FROM bpm_project " +
                        "WHERE deleted = false";
        try (Statement stmt = connection.createStatement()) {
            try (ResultSet resultSet = stmt.executeQuery(getProjectCountSql)) {
                if (resultSet.next()) {
                    Integer countOfComments = resultSet.getInt("projectCount");
                    log.info(String.format("%d Projects found", countOfComments));
                    return countOfComments;
                }
            }
            return 0;
        }
    }
}
