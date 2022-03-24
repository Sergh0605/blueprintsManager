package com.dataart.blueprintsmanager.rest.controller;

import com.dataart.blueprintsmanager.rest.dto.CompanyDto;
import com.dataart.blueprintsmanager.rest.service.CompanyRestService;
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

    @GetMapping
    public ResponseEntity<?> getCompaniesList() {
        return ResponseEntity.ok(companyRestService.getAll());
    }

    @GetMapping("/{companyId}")
    public ResponseEntity<?> getCompany(@PathVariable Long companyId) {
        return ResponseEntity.ok(companyRestService.getById(companyId));
    }

    @PutMapping("/{companyId}")
    public ResponseEntity<?> updateCompany(@PathVariable Long companyId,
                                           @RequestPart @Valid CompanyDto companyDto,
                                           @RequestPart(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.ok(companyRestService.update(companyId, companyDto, file));
    }

    @PostMapping
    public ResponseEntity<?> createCompany(@RequestPart @Valid CompanyDto companyDto,
                                           @RequestPart(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(companyRestService.createCompany(companyDto, file));
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
