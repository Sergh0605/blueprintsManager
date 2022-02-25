package com.dataart.blueprintsmanager;

import com.dataart.blueprintsmanager.persistence.repository.ProjectRepository;
import com.dataart.blueprintsmanager.service.DocumentService;
import com.dataart.blueprintsmanager.service.ProjectService;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@SpringBootTest
class BlueprintsManagerApplicationTests {
    @Autowired
    DataSource dataSource;
    @Autowired
    ProjectService projectService;
    @Autowired
    DocumentService documentService;
    @Autowired
    ProjectRepository projectRepository;

    @AfterEach
    void dropDbChanges() {
        String dropAllSql =
                "DELETE FROM bpm_document; " +
                        "DELETE FROM bpm_project; " +
                        "DELETE FROM bpm_comment;";
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(dropAllSql);
        } catch (SQLException ignored) {
        }
    }

}
