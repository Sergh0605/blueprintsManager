package com.dataart.blueprintsmanager.rest.mapper;

import com.dataart.blueprintsmanager.persistence.entity.UserActivityEntity;
import com.dataart.blueprintsmanager.rest.dto.ActivityDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserActivityMapper {
    ActivityDto activityEntityToActivityDto(UserActivityEntity entity);
}
