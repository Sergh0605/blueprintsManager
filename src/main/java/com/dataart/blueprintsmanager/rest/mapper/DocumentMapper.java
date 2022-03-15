package com.dataart.blueprintsmanager.rest.mapper;

import com.dataart.blueprintsmanager.persistence.entity.DocumentEntity;
import com.dataart.blueprintsmanager.persistence.entity.DocumentTypeEntity;
import com.dataart.blueprintsmanager.persistence.entity.ProjectEntity;
import com.dataart.blueprintsmanager.persistence.entity.UserEntity;
import com.dataart.blueprintsmanager.rest.dto.BasicDto;
import com.dataart.blueprintsmanager.rest.dto.DocumentDto;
import com.dataart.blueprintsmanager.rest.dto.DocumentTypeDto;
import com.dataart.blueprintsmanager.service.DocumentService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class DocumentMapper {
    @Autowired
    protected DocumentService documentService;

    @Mapping(target = "documentFullCode", expression = "java(documentService.getFullCode(entity))")
    public abstract DocumentDto documentEntityToDocumentDto(DocumentEntity entity);

    public abstract DocumentEntity documentDtoToDocumentEntity(DocumentDto dto);

    public abstract BasicDto projectEntityToBasicDto(ProjectEntity entity);

    public abstract ProjectEntity BasicDtoToProjectEntity(BasicDto dto);

    public abstract DocumentTypeDto documentTypeEntityToDocumentTypeDto(DocumentTypeEntity entity);

    public abstract DocumentTypeEntity documentTypeDtoToDocumentTypeEntity(DocumentTypeDto dto);

    @Mapping(target = "name", source = "entity.lastName")
    public abstract BasicDto userEntityToBasicDto(UserEntity entity);

    public abstract UserEntity BasicDtoToUserEntity(BasicDto dto);

}