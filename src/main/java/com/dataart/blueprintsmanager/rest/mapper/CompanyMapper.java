package com.dataart.blueprintsmanager.rest.mapper;

import com.dataart.blueprintsmanager.persistence.entity.CompanyEntity;
import com.dataart.blueprintsmanager.rest.dto.CompanyDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CompanyMapper {
    CompanyDto companyEntityToCompanyDto(CompanyEntity entity);

    CompanyEntity companyDtoToCompanyEntity(CompanyDto dto);
}
