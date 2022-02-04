package persistence.repository;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class ProjectRepository {
    DataSource dataSource;

    public ProjectRepository(DataSource dataSource) {

    }
}
