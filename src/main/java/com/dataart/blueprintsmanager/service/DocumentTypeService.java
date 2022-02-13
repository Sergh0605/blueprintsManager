package com.dataart.blueprintsmanager.service;

import com.dataart.blueprintsmanager.persistence.repository.DocumentTypeRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class DocumentTypeService {
    DocumentTypeRepository documentTypeRepository;

    public List<byte[]> getDocumentTemplateByTypeId(Long documentTypeId) {
        return documentTypeRepository.fetchPdfTemplatesByIdTransactional(documentTypeId);
    }

}
