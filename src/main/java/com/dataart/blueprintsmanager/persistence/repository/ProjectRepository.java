package com.dataart.blueprintsmanager.persistence.repository;

import com.dataart.blueprintsmanager.persistence.entity.CompanyEntity;
import com.dataart.blueprintsmanager.persistence.entity.ProjectEntity;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ProjectRepository {
    private final DataSource dataSource;

    public ProjectRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<ProjectEntity> findAll() {
        String getAllProjectsSql = "SELECT prj.id, prj.name, prj.code, prj.edit_time, prj.reassembly_required, cmp.name FROM bpm_project prj INNER JOIN bpm_company cmp ON prj.company_id = cmp.id WHERE prj.deleted = 'false'";
        try (Connection connection = dataSource.getConnection();
             ResultSet resultSet = connection.prepareStatement(getAllProjectsSql).executeQuery()) {
            List<ProjectEntity> projectEntityList = new ArrayList<>();
            while (resultSet.next()) {
                ProjectEntity project = ProjectEntity.builder()
                        .id(resultSet.getLong("id"))
                        .name(resultSet.getString(2))
                        .code(resultSet.getString("code"))
                        .editTime(resultSet.getTimestamp("edit_time").toLocalDateTime())
                        .reassemblyRequired(resultSet.getBoolean("reassembly_required"))
                        .company(CompanyEntity.builder().name(resultSet.getString(6)).build())
                        .build();
                projectEntityList.add(project);
            }
            return projectEntityList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
