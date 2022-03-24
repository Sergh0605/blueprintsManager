package com.dataart.blueprintsmanager.rest.controller;

import com.dataart.blueprintsmanager.BlueprintsManagerApplication;
import com.dataart.blueprintsmanager.rest.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = BlueprintsManagerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class UserControllerTest extends BlueprintsManagerTest {

    @Test
    void givenInvalidUserCredentials_whenAuthenticate_then401() {
        //Given
        UserAuthDto userAuthDto = UserAuthDto.builder()
                .login("invalidUser")
                .password("invalidPassword")
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserAuthDto> entity = new HttpEntity<>(userAuthDto, headers);
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        //When
        ResponseEntity<ExceptionDto> response = testRestTemplate.exchange(
                createURLWithPort("/api/user/auth"),
                HttpMethod.POST, entity, ExceptionDto.class);
        //Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(), "Wrong code");
    }

    @Test
    void givenValidAdminCredentials_whenAuthenticate_then200AndJWTsReceived() {
        //Given
        UserAuthDto userAuthDto = UserAuthDto.builder()
                .login("admin")
                .password("admin")
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
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Wrong code");
        assertNotNull(response.getBody(), "Empty body");
        assertNotNull(response.getBody().getAccessToken(), "Empty Access Token");
        assertNotNull(response.getBody().getRefreshToken(), "Empty Refresh Token");
    }

    @Test
    void givenAdminJwt_whenGetAllByAdmin_then200AndPageOfUsersReceived() {
        //Given
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAdminTokens().getAccessToken());
        HttpEntity<UserAuthDto> entity = new HttpEntity<>(null, headers);
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        //When
        ResponseEntity<String> response = testRestTemplate.exchange(
                createURLWithPort("/api/user"),
                HttpMethod.GET, entity, String.class);
        //Then
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Wrong code");
        assertNotNull(response.getBody(), "Empty body");
    }

    @Test
    void givenAdminJwt_whenGetAllByAdminWithWrongPageData_then400AndExceptionReceived() {
        //Given
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAdminTokens().getAccessToken());
        HttpEntity<UserAuthDto> entity = new HttpEntity<>(null, headers);
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        //When
        ResponseEntity<ExceptionDto> response = testRestTemplate.exchange(
                createURLWithPort("/api/user?page=gf&size=sd"),
                HttpMethod.GET, entity, ExceptionDto.class);
        //Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Wrong code");
        assertNotNull(response.getBody(), "Empty body");
        assertEquals(400, response.getBody().getStatus(), "Wrong status");
    }

    @Test
    void givenUserRegParams_whenRegByAdmin_then201AndUserReceived() {
        //Given
        Set<BasicDto> roles = roleRepository.findAll().stream().map(r -> new BasicDto(r.getId())).collect(Collectors.toSet());
        UserRegistrationDto userDto = UserRegistrationDto.builder()
                .login("testUser")
                .password("testUserPassword")
                .company(new BasicDto(companyRepository.findAll().stream().findFirst().get().getId()))
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
        //When
        ResponseEntity<UserDto> response = testRestTemplate.exchange(
                createURLWithPort("/api/user"),
                HttpMethod.POST, entity, UserDto.class);
        //Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Wrong code");
        assertNotNull(response.getBody(), "Empty body");
        assertEquals(userDto.getLogin(), response.getBody().getLogin(), "Login mismatch");
        assertEquals(userDto.getEmail(), response.getBody().getEmail(), "Email mismatch");
        assertEquals(userDto.getLastName(), response.getBody().getLastName(), "LastName mismatch");
        assertEquals(userDto.getCompany().getId(), response.getBody().getCompany().getId(), "Company mismatch");
        assertEquals(roles.size(), response.getBody().getRoles().size(), "Roles mismatch");
    }

    @Test
    void givenUserUpdateParams_whenUpdateByAdmin_then200AndUpdatedUserReceived() {
        //Given
        UserDto createdUser = createNewUser();
        UserRegistrationDto userDto = UserRegistrationDto.builder()
                .login("Updated")
                .company(new BasicDto(0L))
                .email("updated@test.com")
                .lastName("Updated")
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAdminTokens().getAccessToken());
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("user", userDto);
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(map, headers);
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        //When
        ResponseEntity<UserDto> response = testRestTemplate.exchange(
                createURLWithPort("/api/user/" + createdUser.getId()),
                HttpMethod.PUT, entity, UserDto.class);
        //Then
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Wrong code");
        assertNotNull(response.getBody(), "Empty body");
        assertEquals(createdUser.getLogin(), response.getBody().getLogin(), "Login mismatch");
        assertEquals(userDto.getEmail(), response.getBody().getEmail(), "Email mismatch");
        assertEquals(userDto.getLastName(), response.getBody().getLastName(), "LastName mismatch");
        assertEquals(createdUser.getCompany().getId(), response.getBody().getCompany().getId(), "Company mismatch");
    }

    @Test
    void givenValidRefreshJwt_whenAuthenticate_then200AndJWTsReceived() throws InterruptedException {
        //Given
        AuthResponseDto adminTokens = getAdminTokens();
        AuthRequestByTokenDto adminRefreshToken = AuthRequestByTokenDto.builder()
                .token(adminTokens.getRefreshToken())
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AuthRequestByTokenDto> entity = new HttpEntity<>(adminRefreshToken, headers);
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        Thread.sleep(5000);
        //When
        ResponseEntity<AuthResponseDto> response = testRestTemplate.exchange(
                createURLWithPort("/api/user/refresh_token_auth"),
                HttpMethod.POST, entity, AuthResponseDto.class);
        //Then
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Wrong code");
        assertNotNull(response.getBody(), "Empty body");
        assertNotNull(response.getBody().getAccessToken(), "Empty Access Token");
        assertNotNull(response.getBody().getRefreshToken(), "Empty Refresh Token");
        assertNotEquals(adminTokens.getAccessToken(), response.getBody().getAccessToken(), "Access Token are the same");
        assertNotEquals(adminTokens.getRefreshToken(), response.getBody().getRefreshToken(), "Refresh Token are the same");
        assertTrue(tokenRepository.findById(adminTokens.getAccessToken()).get().getDisabled(), "Old Access Token wasn't disabled in DB");
        assertTrue(tokenRepository.findById(adminTokens.getRefreshToken()).get().getDisabled(), "Old Refresh Token wasn't disabled in DB");
    }

    @Test
    void givenValidAccessJwt_whenLogout_then200AndJWTsDisabled() {
        //Given
        AuthResponseDto adminTokens = getAdminTokens();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminTokens.getAccessToken());
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        //When
        ResponseEntity<String> response = testRestTemplate.exchange(
                createURLWithPort("/api/user/logout"),
                HttpMethod.POST, entity, String.class);
        //Then
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Wrong code");
        assertTrue(tokenRepository.findById(adminTokens.getAccessToken()).get().getDisabled(), "Old Access Token wasn't disabled in DB");
        assertTrue(tokenRepository.findById(adminTokens.getRefreshToken()).get().getDisabled(), "Old Refresh Token wasn't disabled in DB");
    }

    @Test
    void givenValidAccessJwt_whenLogoutWithWrongMethod_then400AndExceptionReceived() {
        //Given
        AuthResponseDto adminTokens = getAdminTokens();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminTokens.getAccessToken());
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        //When
        ResponseEntity<String> response = testRestTemplate.exchange(
                createURLWithPort("/api/user/logout"),
                HttpMethod.GET, entity, String.class);
        //Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Wrong code");
        assertNotNull(response.getBody(), "Empty body");
    }
}