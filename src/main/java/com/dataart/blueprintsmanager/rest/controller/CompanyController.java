package com.dataart.blueprintsmanager.rest.controller;

import com.dataart.blueprintsmanager.rest.dto.CompanyDto;
import com.dataart.blueprintsmanager.rest.service.CompanyRestService;
import com.dataart.blueprintsmanager.rest.service.UserRestService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@Controller
@RequestMapping("/api/company")
@Slf4j
@AllArgsConstructor
public class CompanyController {
    private final CompanyRestService companyRestService;
    private final UserRestService userRestService;

    @GetMapping
    public ResponseEntity<?> getCompaniesList(
            @RequestParam(name = "search", defaultValue = "") String search) {
        return ResponseEntity.ok(companyRestService.getAll(search));
    }

    @GetMapping("/{companyId}")
    public ResponseEntity<?> getCompany(@PathVariable Long companyId) {
        return ResponseEntity.ok(companyRestService.getById(companyId));
    }

    @GetMapping("/{companyId}/users")
    public ResponseEntity<?> getUsersByCompanyId(@PathVariable Long companyId) {
        return ResponseEntity.ok(userRestService.getUsersByCompanyId(companyId));
    }

    @PutMapping("/{companyId}")
    public ResponseEntity<?> updateCompany(@PathVariable Long companyId,
                                           @RequestPart @Valid CompanyDto company,
                                           @RequestPart(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.ok(companyRestService.update(companyId, company, file));
    }

    @PostMapping
    public ResponseEntity<?> createCompany(@RequestPart @Valid CompanyDto company,
                                           @RequestPart(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(companyRestService.createCompany(company, file));
    }

    @PostMapping(value = {"/{companyId}/disable"})
    public ResponseEntity<?> deleteCompany(@PathVariable Long companyId) {
        companyRestService.deleteById(companyId);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = {"/{companyId}/enable"})
    public ResponseEntity<?> restoreCompany(@PathVariable Long companyId) {
        companyRestService.restoreById(companyId);
        return ResponseEntity.ok().build();
    }
}
