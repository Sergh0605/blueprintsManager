package com.dataart.blueprintsmanager.rest.mapper;

import com.dataart.blueprintsmanager.persistence.entity.CompanyEntity;
import com.dataart.blueprintsmanager.persistence.entity.UserEntity;
import com.dataart.blueprintsmanager.rest.dto.BasicDto;
import com.dataart.blueprintsmanager.rest.dto.UserDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto userEntityToUserDto(UserEntity entity);

    UserEntity userDtoToUserEntity(UserDto dto);

    BasicDto companyEntityToBasicDto(CompanyEntity entity);

    CompanyEntity BasicDtoToCompanyEntity(BasicDto dto);
}
