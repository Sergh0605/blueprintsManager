package com.dataart.blueprintsmanager.service;

import com.dataart.blueprintsmanager.dto.DocumentDto;
import com.dataart.blueprintsmanager.persistence.entity.DocumentEntity;
import com.dataart.blueprintsmanager.persistence.entity.DocumentType;
import com.dataart.blueprintsmanager.persistence.entity.ProjectEntity;
import com.dataart.blueprintsmanager.persistence.repository.DocumentRepository;
import com.dataart.blueprintsmanager.persistence.repository.ProjectRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class DocumentService {
    DocumentRepository documentRepository;
    ProjectRepository projectRepository;

    private DocumentDto createCoverPage(Long projectId) {
        ProjectEntity project = projectRepository.fetchByIdTransactional(projectId);
        DocumentEntity document = DocumentEntity.builder()
                .project(project)
                .numberInProject(1)
                .documentType(DocumentType.COVER_PAGE)
                .name("Обложка")
                .reassemblyRequired(true)
                .editTime(LocalDateTime.now())
                .build();

        return new DocumentDto();
    }

    private void reassembleCoverPage() {

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
            documents = documentRepository.fetchAllByProjectIdTransactional(projectId);
        }
        return documents.stream().
                filter(Objects::nonNull).
                map(DocumentDto::new).
                collect(Collectors.toList());
    }

    public DocumentDto getById(Long documentId){
        return new DocumentDto(documentRepository.fetchByIdTransactional(documentId));
    }
}
