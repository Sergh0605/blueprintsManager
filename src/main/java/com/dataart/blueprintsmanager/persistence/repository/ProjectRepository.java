package com.dataart.blueprintsmanager.persistence.repository;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.SQLException;

@Repository
public class ProjectRepository {
    DataSource dataSource;

    public ProjectRepository(DataSource dataSource) throws SQLException {
/*        Connection connection = dataSource.getConnection();
        String stmt = "INSERT INTO public.bpm_company (name, signer_position, signer_name) VALUES ('ООО \"Рога и Копыта\"', 'Исполнительный директор', 'Иванов И.И.')";
        connection.createStatement().execute(stmt);*/
    }
}
