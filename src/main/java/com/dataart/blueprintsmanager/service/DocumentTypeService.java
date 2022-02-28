package com.dataart.blueprintsmanager.service;

import com.dataart.blueprintsmanager.persistence.entity.DocumentType;
import com.dataart.blueprintsmanager.persistence.repository.DocumentTypeRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class DocumentTypeService {
    private final DocumentTypeRepository documentTypeRepository;

    public byte[] getFirstPageTemplateByTypeId(Long documentTypeId) {
        return documentTypeRepository.fetchFirstPageTemplateById(documentTypeId);
    }

    public byte[] getGeneralPageTemplateByTypeId(Long documentTypeId) {
        return documentTypeRepository.fetchGeneralPageTemplateById(documentTypeId);
    }

    public List<DocumentType> getAll() {
        return Arrays.stream(DocumentType.values()).toList();
    }
}
