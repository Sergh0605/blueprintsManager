package com.dataart.blueprintsmanager.rest.mapper;

import com.dataart.blueprintsmanager.persistence.entity.DocumentTypeEntity;
import com.dataart.blueprintsmanager.rest.dto.DocumentTypeDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DocumentTypeMapper {
    DocumentTypeDto documentTypeEntityToDocumentTypeDto(DocumentTypeEntity entity);

    DocumentTypeEntity documentTypeDtoToDocumentTypeEntity(DocumentTypeDto dto);
}