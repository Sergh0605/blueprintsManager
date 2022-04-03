package com.dataart.blueprintsmanager.rest.service;

import com.dataart.blueprintsmanager.persistence.entity.DocumentType;
import com.dataart.blueprintsmanager.persistence.entity.DocumentTypeEntity;
import com.dataart.blueprintsmanager.persistence.entity.RoleEntity;
import com.dataart.blueprintsmanager.rest.dto.DocumentTypeDto;
import com.dataart.blueprintsmanager.rest.dto.RoleDto;
import com.dataart.blueprintsmanager.rest.mapper.DocumentTypeMapper;
import com.dataart.blueprintsmanager.service.DocumentTypeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class DocTypeRestService {
    private final DocumentTypeService documentTypeService;
    private final DocumentTypeMapper documentTypeMapper;

    public List<DocumentTypeDto> getAllDocTypes() {
        log.info("Try to get all Document Types");
        List<DocumentTypeEntity> documentTypeEntities = documentTypeService.getAllModified();
        List<DocumentTypeDto> documentTypeDtos = documentTypeEntities.stream().map(documentTypeMapper::documentTypeEntityToDocumentTypeDto).toList();
        log.info("{} Document Types found.", documentTypeDtos.size());
        return documentTypeDtos;
    }
}
