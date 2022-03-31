package com.dataart.blueprintsmanager.service;

import com.dataart.blueprintsmanager.exceptions.InvalidInputDataException;
import com.dataart.blueprintsmanager.exceptions.NotFoundCustomApplicationException;
import com.dataart.blueprintsmanager.persistence.entity.CompanyEntity;
import com.dataart.blueprintsmanager.persistence.entity.FileEntity;
import com.dataart.blueprintsmanager.persistence.repository.CompanyRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {
    private final CompanyRepository companyRepository;

    public List<CompanyEntity> getAllNotDeleted() {
        return companyRepository.findAllByDeletedOrderByName(false);
    }

    public CompanyEntity getById(Long companyId) {
        return companyRepository.findById(companyId).orElseThrow(() -> {
            throw new NotFoundCustomApplicationException(String.format("Company with ID %d not found", companyId));
        });
    }

    @Transactional
    public CompanyEntity create(CompanyEntity company, MultipartFile logoFile) {
        if (existsByName(company.getName())) {
            throw new InvalidInputDataException(String.format("Can't create Company. Company with NAME = %s is already exists", company.getName()));
        }
        company.setId(null);
        company.setDeleted(false);
        company.setLogoFile(new FileEntity());
        setLogoToCompany(company, logoFile);
        return companyRepository.save(company);
    }

    @Transactional
    public CompanyEntity update(CompanyEntity companyForUpdate, MultipartFile logoFile) {
        CompanyEntity currentCompany = getById(companyForUpdate.getId());
        if (!currentCompany.getName().equals(companyForUpdate.getName())) {
            if (existsByName(companyForUpdate.getName())) {
                throw new InvalidInputDataException(String.format("Can't save Company. Company with NAME = %s is already exists", companyForUpdate.getName()));
            }
        }
        currentCompany.setName(companyForUpdate.getName());
        currentCompany.setSignerPosition(companyForUpdate.getSignerPosition());
        currentCompany.setSignerName(companyForUpdate.getSignerName());
        currentCompany.setCity(companyForUpdate.getCity());
        setLogoToCompany(currentCompany, logoFile);
        return companyRepository.save(currentCompany);
    }

    @Transactional
    public void setDeletedById(Long id, Boolean deleteStatus) {
        CompanyEntity companyForDelete = getById(id);
        companyForDelete.setDeleted(deleteStatus);
        companyRepository.save(companyForDelete);
    }

    private Boolean existsByName(String name) {
        return companyRepository.existsByName(name);
    }

    private void setLogoToCompany(CompanyEntity company, MultipartFile logoFile) {
        if (logoFile != null) {
            if (!logoFile.isEmpty() && logoFile.getContentType().contains("image")) {
                try {
                    company.getLogoFile().setFileInBytes(logoFile.getInputStream().readAllBytes());
                } catch (IOException e) {
                    log.debug(e.getMessage(), e);
                    throw new InvalidInputDataException(String.format("Can't save Company with NAME = %s. Broken logo file", company.getName()), e);
                }
            } else {
                throw new InvalidInputDataException("Can't save company. Wrong type of logo file.");
            }
        }
    }
}
