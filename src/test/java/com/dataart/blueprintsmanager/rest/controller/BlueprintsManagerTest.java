package com.dataart.blueprintsmanager.rest.controller;

import com.dataart.blueprintsmanager.persistence.repository.*;
import com.dataart.blueprintsmanager.rest.dto.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;


public class BlueprintsManagerTest {
    @Autowired
    private DataSource dataSource;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    TokenRepository tokenRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    DocumentRepository documentRepository;

    @BeforeEach
    void initDb() throws Exception {
        executeSql("initDb.sql");
    }

    @AfterEach
    void dropData() throws Exception {
        executeSql("dropData.sql");
    }

    private void executeSql(String fileName) throws Exception {
        String sqlPath = "db/changelog/";
        Resource resource = new ClassPathResource(sqlPath + fileName);
        String initSql = new String(Files.readAllBytes(resource.getFile().toPath()));
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(initSql);
        }
    }

    protected String createURLWithPort(String uri) {
        return "http://localhost:" + "8085" + uri;
    }

    protected AuthResponseDto getUserTokens(String login, String password) {
        UserAuthDto userAuthDto = UserAuthDto.builder()
                .login(login)
                .password(password)
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserAuthDto> entity = new HttpEntity<>(userAuthDto, headers);
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        //When
        ResponseEntity<AuthResponseDto> response = testRestTemplate.exchange(
                createURLWithPort("/api/user/auth"),
                HttpMethod.POST, entity, AuthResponseDto.class);
        //Then
        return response.getBody();
    }

    protected AuthResponseDto getAdminTokens() {
        return getUserTokens("admin", "admin");
    }

    protected AuthResponseDto getEditorTokens() {
        return getUserTokens("editor", "editor");
    }

    protected UserDto createNewUser() {
        Set<BasicDto> roles = roleRepository.findAll().stream().map(r -> new BasicDto(r.getId())).collect(Collectors.toSet());
        UserRegistrationDto userDto = UserRegistrationDto.builder()
                .login("testUser")
                .company(new BasicDto(companyRepository.findAll().stream().findFirst().get().getId()))
                .password("testPassword")
                .email("test@test.com")
                .lastName("Popov")
                .roles(roles)
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAdminTokens().getAccessToken());
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("user", userDto);
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(map, headers);
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        ResponseEntity<UserDto> response = testRestTemplate.exchange(
                createURLWithPort("/api/user"),
                HttpMethod.POST, entity, UserDto.class);
        return response.getBody();
    }

    protected ProjectDto getValidProjectData() {
        return ProjectDto.builder()
                .name("Тестовый проект")
                .code("Код")
                .releaseDate(LocalDate.now())
                .volumeName("Тестовый том")
                .volumeNumber(1L)
                .company(new BasicDto(companyRepository.findAll().get(0).getId()))
                .stage(new BasicDto(1L))
                .build();
    }

    protected ProjectDto getInvalidProjectData() {
        return ProjectDto.builder()
                .name("")
                .company(new BasicDto(companyRepository.findAll().get(0).getId()))
                .stage(new BasicDto(1L))
                .build();
    }

    protected <T> ResponseEntity<T> createProject(ProjectDto projectDataForCreation, String jwt, Class<T> tClass) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwt);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ProjectDto> entity = new HttpEntity<>(projectDataForCreation, headers);
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        return testRestTemplate.exchange(
                createURLWithPort("/api/project"),
                HttpMethod.POST, entity, tClass);
    }

    protected <T> ResponseEntity<T> updateProject(ProjectDto projectDataForUpdate, String jwt, Class<T> tClass) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwt);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ProjectDto> entity = new HttpEntity<>(projectDataForUpdate, headers);
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        return testRestTemplate.exchange(
                createURLWithPort("/api/project/" + projectDataForUpdate.getId()),
                HttpMethod.PUT, entity, tClass);
    }

    protected <T> ResponseEntity<T> reassembleProject(Long projectId, String jwt, Class<T> tClass) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwt);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ProjectDto> entity = new HttpEntity<>(null, headers);
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        return testRestTemplate.exchange(
                createURLWithPort("/api/project/" + projectId + "/assemble"),
                HttpMethod.POST, entity, tClass);
    }
}
