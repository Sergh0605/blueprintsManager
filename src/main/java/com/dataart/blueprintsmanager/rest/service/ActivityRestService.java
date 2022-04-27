package com.dataart.blueprintsmanager.rest.service;

import com.dataart.blueprintsmanager.persistence.entity.UserActivityEntity;
import com.dataart.blueprintsmanager.rest.dto.ActivityDto;
import com.dataart.blueprintsmanager.rest.mapper.UserActivityMapper;
import com.dataart.blueprintsmanager.service.UserActivityService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class ActivityRestService {
    private final UserActivityService activityService;
    private final UserActivityMapper activityMapper;

    public Page<ActivityDto> getAllFilteredPaginated(String search, Pageable pageable) {
        log.info("Try to get {} page with {} activities with filter = {}", pageable.getPageNumber(), pageable.getPageSize(), search);
        Page<UserActivityEntity> userActivityEntityPage = activityService.getAllFilteredPaginated(search, pageable);
        List<ActivityDto> activityDtoList = userActivityEntityPage.getContent().stream().map(activityMapper::activityEntityToActivityDto).toList();
        Page<ActivityDto> activityDtoPage = new PageImpl<>(activityDtoList, pageable, userActivityEntityPage.getTotalElements());
        log.info("Page {} with {} activities found with filter = {}", activityDtoPage.getNumber(), activityDtoPage.getNumberOfElements(), search);
        return activityDtoPage;
    }
}
