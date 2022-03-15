package com.dataart.blueprintsmanager.rest.service;

import com.dataart.blueprintsmanager.persistence.entity.CompanyEntity;
import com.dataart.blueprintsmanager.rest.dto.CompanyDto;
import com.dataart.blueprintsmanager.rest.mapper.CompanyMapper;
import com.dataart.blueprintsmanager.service.CompanyService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class CompanyRestService {
    private final CompanyService companyService;
    private final CompanyMapper companyMapper;

    public List<CompanyDto> getAll() {
        log.info("Try to find all companies");
        List<CompanyEntity> companyEntities = companyService.getAllNotDeleted();
        List<CompanyDto> companyDtos = companyEntities.stream().map(companyMapper::companyEntityToCompanyDto).toList();
        log.info("{} companies found", companyDtos.size());
        return companyDtos;
    }

    public CompanyDto getById(Long companyId) {
        log.info("Try to find company with ID = {}", companyId);
        CompanyEntity company = companyService.getById(companyId);
        CompanyDto companyDto = companyMapper.companyEntityToCompanyDto(company);
        log.info("Company with ID = {} found", companyId);
        return companyDto;
    }

    public CompanyDto update(Long companyId, CompanyDto companyDto, MultipartFile logoFile){
        log.info("Try to update Company with ID = {}", companyId);
        companyDto.setId(companyId);
        CompanyEntity updatedCompany = companyService.update(companyMapper.companyDtoToCompanyEntity(companyDto), logoFile);
        CompanyDto updatedCompanyDto = companyMapper.companyEntityToCompanyDto(updatedCompany);
        log.info("Company with ID = {} updated", companyId);
        return updatedCompanyDto;
    }

    public CompanyDto createCompany(CompanyDto companyDto, MultipartFile file) {
        log.info("Try to create new Company with NAME = {}", companyDto.getName());
        CompanyEntity createdCompany = companyService.create(companyMapper.companyDtoToCompanyEntity(companyDto), file);
        CompanyDto createdCompanyDto = companyMapper.companyEntityToCompanyDto(createdCompany);
        log.info("Company with NAME {} created with ID = {}", createdCompanyDto.getName(), createdCompanyDto.getId());
        return createdCompanyDto;
    }

    public void deleteById(Long companyId) {
        log.info("Try to mark as deleted Company with ID = {}", companyId);
        companyService.setDeletedById(companyId, true);
        log.info("Company with ID = {} marked as deleted.", companyId);
    }

    public void restoreById(Long companyId) {
        log.info("Try to restore Company with ID = {}", companyId);
        companyService.setDeletedById(companyId, false);
        log.info("Project with ID = {} restored.", companyId);
    }
}
