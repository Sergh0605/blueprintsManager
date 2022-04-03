package com.dataart.blueprintsmanager.service;

import com.dataart.blueprintsmanager.exceptions.NotFoundCustomApplicationException;
import com.dataart.blueprintsmanager.persistence.entity.DocumentType;
import com.dataart.blueprintsmanager.persistence.entity.DocumentTypeEntity;
import com.dataart.blueprintsmanager.persistence.repository.DocumentTypeRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
@Transactional(readOnly = true)
public class DocumentTypeService {
    private final DocumentTypeRepository documentTypeRepository;

    public List<DocumentTypeEntity> getAllModified() {
        return documentTypeRepository.findAllByUnmodified(false);
    }

    public DocumentTypeEntity getByType(DocumentType type) {
        return documentTypeRepository.findByType(type).orElseThrow(() -> {
            throw new NotFoundCustomApplicationException(String.format("Document type with type %s not found", type));
        });
    }

    public DocumentTypeEntity getById(Long id) {
        return documentTypeRepository.findById(id).orElseThrow(() -> {
            throw new NotFoundCustomApplicationException(String.format("Document type with ID %d not found", id));
        });
    }
}
