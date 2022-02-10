package com.dataart.blueprintsmanager.service;

import com.dataart.blueprintsmanager.dto.DocumentDto;
import com.dataart.blueprintsmanager.persistence.entity.DocumentEntity;
import com.dataart.blueprintsmanager.persistence.repository.DocumentRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class DocumentService {
    DocumentRepository documentRepository;

    private List<DocumentDto> toDtoListConverter(List<DocumentEntity> documentEntities) {
        return documentEntities.stream().
                filter(Objects::nonNull).
                map(DocumentDto::new).
                collect(Collectors.toList());
    }

    private DocumentDto createCoverPage(DocumentDto document) {
        return new DocumentDto();
    }

    private DocumentDto createTitlePage(DocumentDto document) {
        return new DocumentDto();
    }

    private DocumentDto createTableOfContents(DocumentDto document) {
        return new DocumentDto();
    }

    private DocumentDto createGeneralInfo(DocumentDto document, byte[] text) {
        return new DocumentDto();
    }

    private DocumentDto createDrawing(DocumentDto document, byte[] drawing) {
        return new DocumentDto();
    }

    public List<DocumentDto> getAllByProjectId(Long projectId) {
        List<DocumentEntity> documents = new ArrayList<>();
        if (projectId != null) {
            documents = documentRepository.findAllByProjectId(projectId);
        }
        return documents.stream().
                filter(Objects::nonNull).
                map(DocumentDto::new).
                collect(Collectors.toList());
    }
}
