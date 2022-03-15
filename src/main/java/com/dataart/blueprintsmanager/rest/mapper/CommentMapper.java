package com.dataart.blueprintsmanager.rest.mapper;

import com.dataart.blueprintsmanager.persistence.entity.CommentEntity;
import com.dataart.blueprintsmanager.persistence.entity.DocumentEntity;
import com.dataart.blueprintsmanager.persistence.entity.ProjectEntity;
import com.dataart.blueprintsmanager.persistence.entity.UserEntity;
import com.dataart.blueprintsmanager.rest.dto.BasicDto;
import com.dataart.blueprintsmanager.rest.dto.CommentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    CommentDto commentEntityToCommentDto(CommentEntity entity);

    CommentEntity commentDtoToCommentEntity(CommentDto dto);

    @Mapping(target = "name", source = "entity.login")
    BasicDto userEntityToBasicDto(UserEntity entity);

    UserEntity BasicDtoToUserEntity(BasicDto dto);

    @Mapping(target = "name", source = "entity.code")
    BasicDto ProjectEntityToBasicDto(ProjectEntity entity);

    ProjectEntity BasicDtoToProjectEntity(BasicDto dto);

    BasicDto DocumentEntityToBasicDto(DocumentEntity entity);

    DocumentEntity BasicDtoToDocumentEntity(BasicDto dto);
}
