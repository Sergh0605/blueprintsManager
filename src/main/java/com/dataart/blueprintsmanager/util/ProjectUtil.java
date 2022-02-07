package com.dataart.blueprintsmanager.util;

import com.dataart.blueprintsmanager.dto.ProjectDto;
import com.dataart.blueprintsmanager.persistence.entity.ProjectEntity;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProjectUtil {
    public static List<ProjectDto> toDtoListConverter(List<ProjectEntity> projectEntities) {
        return projectEntities.stream().
                filter(Objects::nonNull).
                map(ProjectDto::new).
                collect(Collectors.toList());
    }
}
