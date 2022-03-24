package com.dataart.blueprintsmanager.rest.controller;

import com.dataart.blueprintsmanager.BlueprintsManagerApplication;
import com.dataart.blueprintsmanager.rest.dto.CompanyDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = BlueprintsManagerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class CompanyControllerTest extends BlueprintsManagerTest {

    @Test
    void givenAdminJwt_whenGetAllByAdmin_then200AndPageOfCompaniesReceived() {
        //Given
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAdminTokens().getAccessToken());
        HttpEntity<CompanyDto> entity = new HttpEntity<>(null, headers);
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        long totalCompaniesCount = companyRepository.count();
        //When
        ResponseEntity<List<CompanyDto>> response = testRestTemplate.exchange(
                createURLWithPort("/api/company"),
                HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});
        //Then
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Wrong code");
        assertNotNull(response.getBody(), "Empty body");
        assertEquals(totalCompaniesCount, response.getBody().size(), "Wrong count of companies");
    }

    @Test
    void givenCompanyData_whenCreateByAdmin_then201AndCompanyReceived() {
        //Given
        CompanyDto companyDto = CompanyDto.builder()
                .name("Тестовая компания")
                .signerPosition("Директор")
                .signerName("Попов")
                .city("Город")
                .build();
        long dbCompaniesCountBefore = companyRepository.count();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAdminTokens().getAccessToken());
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("companyDto", companyDto);
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(map, headers);
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        //When
        ResponseEntity<CompanyDto> response = testRestTemplate.exchange(
                createURLWithPort("/api/company"),
                HttpMethod.POST, entity, CompanyDto.class);
        long dbCompaniesCountAfter = companyRepository.count();
        //Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Wrong code");
        assertNotNull(response.getBody(), "Empty body");
        assertEquals(companyDto.getName(), response.getBody().getName(), "Company name mismatch");
        assertEquals(dbCompaniesCountBefore + 1, dbCompaniesCountAfter, "DB Count of companies mismatch");
    }
}
