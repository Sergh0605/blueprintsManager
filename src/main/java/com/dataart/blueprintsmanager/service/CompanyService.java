package com.dataart.blueprintsmanager.service;

import com.dataart.blueprintsmanager.dto.CompanyDto;
import com.dataart.blueprintsmanager.persistence.entity.CompanyEntity;
import com.dataart.blueprintsmanager.persistence.repository.CompanyRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class CompanyService {
    private final CompanyRepository companyRepository;

    public List<CompanyDto> getAll() {
        List<CompanyEntity> companyEntities = companyRepository.fetchAll();
        return toDtoListConverter(companyEntities);
    }

    public CompanyDto getById(Long companyId) {
        CompanyEntity company = companyRepository.fetchById(companyId);
        return new CompanyDto(company);
    }

    public CompanyEntity getEntityById(Long companyId) {
        return companyRepository.fetchById(companyId);
    }

    private List<CompanyDto> toDtoListConverter(List<CompanyEntity> companyEntities) {
        return companyEntities.stream().
                filter(Objects::nonNull).
                map(CompanyDto::new).
                collect(Collectors.toList());
    }
}
