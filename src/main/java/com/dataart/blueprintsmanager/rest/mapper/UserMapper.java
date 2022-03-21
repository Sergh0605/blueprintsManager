package com.dataart.blueprintsmanager.rest.mapper;

import com.dataart.blueprintsmanager.persistence.entity.CompanyEntity;
import com.dataart.blueprintsmanager.persistence.entity.UserEntity;
import com.dataart.blueprintsmanager.rest.dto.BasicDto;
import com.dataart.blueprintsmanager.rest.dto.UserAuthDto;
import com.dataart.blueprintsmanager.rest.dto.UserDto;
import com.dataart.blueprintsmanager.rest.dto.UserRegistrationDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto userEntityToUserDto(UserEntity entity);

    UserEntity userDtoToUserEntity(UserDto dto);

    UserEntity userRegDtoToUserEntity(UserRegistrationDto dto);

    UserEntity userAuthDtoToUserEntity(UserAuthDto dto);

    BasicDto companyEntityToBasicDto(CompanyEntity entity);

    CompanyEntity BasicDtoToCompanyEntity(BasicDto dto);

}
