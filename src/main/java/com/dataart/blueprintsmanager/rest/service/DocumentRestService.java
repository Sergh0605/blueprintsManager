package com.dataart.blueprintsmanager.rest.service;

import com.dataart.blueprintsmanager.aop.track.ParamName;
import com.dataart.blueprintsmanager.aop.track.UserAction;
import com.dataart.blueprintsmanager.aop.track.UserActivityTracker;
import com.dataart.blueprintsmanager.persistence.entity.DocumentEntity;
import com.dataart.blueprintsmanager.rest.dto.BasicDto;
import com.dataart.blueprintsmanager.rest.dto.DocumentDto;
import com.dataart.blueprintsmanager.rest.mapper.DocumentMapper;
import com.dataart.blueprintsmanager.service.DocumentService;
import com.dataart.blueprintsmanager.service.DocumentTypeService;
import com.dataart.blueprintsmanager.service.ProjectService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class DocumentRestService {
    DocumentService documentService;
    DocumentTypeService documentTypeService;
    DocumentMapper documentMapper;
    ProjectService projectService;

    @UserActivityTracker(action = UserAction.DOWNLOAD_DOCUMENT, documentId = "#documentId.toString()", projectId = "#projectId.toString()")
    public void getFileForDownload(@ParamName("projectId") Long projectId, @ParamName("documentId") Long documentId, HttpServletResponse response) {
        log.info("Try to find document in PDF for download. Project ID = {}, Document ID = {}", projectId, documentId);
        documentService.getFileForDownload(documentId, projectId, response);
        log.info("Document in PDF for download found. Project ID = {}, Document ID = {}", projectId, documentId);
    }

    @UserActivityTracker(action = UserAction.DELETE_DOCUMENT, documentId = "#documentId.toString()")
    public void deleteById(@ParamName("documentId") Long projectId, Long documentId) {
        log.info("Try to mark as deleted Document with ID = {} in Project with ID = {}", documentId, projectId);
        documentService.setDeleted(projectId, documentId, true);
        log.info("Document with ID = {} marked as deleted.", documentId);
    }

    @UserActivityTracker(action = UserAction.RESTORE_DOCUMENT, documentId = "#documentId.toString()")
    public void restoreById(@ParamName("documentId") Long projectId, Long documentId) {
        log.info("Try to restore Document with ID = {} in Project with ID = {}", documentId, projectId);
        documentService.setDeleted(projectId, documentId, false);
        log.info("Project with ID = {} restored.", projectId);
    }

    @UserActivityTracker(action = UserAction.REASSEMBLY_DOCUMENT, documentId = "#documentId.toString()")
    public DocumentDto assemble(Long projectId, @ParamName("documentId") Long documentId) {
        log.info("Try to assemble Document with ID = {} in Project with ID = {}", documentId, projectId);
        DocumentEntity reassembledDocument = documentService.reassembleDocument(documentService.getByIdAndProjectId(documentId, projectId));
        DocumentDto reassembledDocumentDto = documentMapper.documentEntityToDocumentDto(reassembledDocument);
        log.info("Document with ID = {} reassembled", projectId);
        return reassembledDocumentDto;
    }

    public DocumentDto getByIdAndProjectId(Long projectId, Long documentId) {
        log.info("Try to find Document with ID = {} in Project with ID = {}", documentId, projectId);
        DocumentEntity document = documentService.getByIdAndProjectId(documentId, projectId);
        DocumentDto documentDto = documentMapper.documentEntityToDocumentDto(document);
        log.info("Document with ID = {} found", projectId);
        return documentDto;
    }

    @UserActivityTracker(action = UserAction.UPDATE_DOCUMENT, documentId = "#documentId.toString()")
    public DocumentDto update(Long projectId, @ParamName("documentId") Long documentId, DocumentDto documentDto, MultipartFile file) {
        log.info("Try to update Document with ID = {} in Project with ID = {}", documentId, projectId);
        documentDto.setId(documentId);
        documentDto.setProject(new BasicDto(projectId));
        DocumentEntity updatedDocument = documentService.update(documentMapper.documentDtoToDocumentEntity(documentDto), file);
        DocumentDto updatedDocumentDto = documentMapper.documentEntityToDocumentDto(updatedDocument);
        log.info("Document with ID = {} updated", projectId);
        return updatedDocumentDto;
    }

    public List<DocumentDto> getByProjectId(Long projectId) {
        log.info("Try to find Documents in Project with ID = {}", projectId);
        projectService.getById(projectId);
        List<DocumentEntity> documentEntities = documentService.getAllByProjectId(projectId);
        List<DocumentDto> documentDtos = documentEntities.stream()
                .map(d -> documentMapper.documentEntityToDocumentDto(d))
                .toList();
        log.info("{} Documents found in Project with ID = {}", documentDtos.size(), projectId);
        return documentDtos;
    }

    @UserActivityTracker(action = UserAction.CREATE_DOCUMENT, documentName = "#document.getName()", projectId = "#projectId.toString()")
    public DocumentDto create(@ParamName("projectId") Long projectId, @ParamName("document") DocumentDto documentDto, MultipartFile file) {
        log.info("Try to create new Document with TYPE = {}", documentTypeService.getById(documentDto.getDocumentType().getId()).getType());
        documentDto.setProject(new BasicDto(projectId));
        DocumentEntity createdDocument = projectService.addNewEditableDocument(documentMapper.documentDtoToDocumentEntity(documentDto),file);
        DocumentDto createdDocumentDto = documentMapper.documentEntityToDocumentDto(createdDocument);
        log.info("Document with ID = {} created in Project with ID = {}", createdDocumentDto.getId(), createdDocumentDto.getProject().getId());
        return createdDocumentDto;
    }
}
