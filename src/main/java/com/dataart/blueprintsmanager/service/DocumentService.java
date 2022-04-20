package com.dataart.blueprintsmanager.service;

import com.dataart.blueprintsmanager.email.EmailService;
import com.dataart.blueprintsmanager.exceptions.InvalidInputDataException;
import com.dataart.blueprintsmanager.exceptions.NotFoundCustomApplicationException;
import com.dataart.blueprintsmanager.pdf.PdfDocumentGenerator;
import com.dataart.blueprintsmanager.pdf.RowOfContentsDocument;
import com.dataart.blueprintsmanager.persistence.entity.*;
import com.dataart.blueprintsmanager.persistence.repository.DocumentRepository;
import com.dataart.blueprintsmanager.persistence.repository.ProjectRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.dataart.blueprintsmanager.pdf.DocumentDataForPdf.getDocumentDataForPdf;
import static com.dataart.blueprintsmanager.persistence.entity.DocumentType.*;
import static com.dataart.blueprintsmanager.util.ResponseUtil.getFile;

@Service
@Slf4j
@Transactional(readOnly = true)
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final DocumentTypeService documentTypeService;
    private final UserService userService;
    private final String pathForPdfFont;
    private final EmailService emailService;
    private final String pdfFileNameTemplate;
    private final String fullDocumentCodeTemplate;
    private final ProjectRepository projectRepository;
    private final EntityManager entityManager;

    public DocumentService(DocumentRepository documentRepository,
                           DocumentTypeService documentTypeService,
                           UserService userService,
                           @Value("${bpm.pdf.fontfilepath}") String pathForPdfFont,
                           EmailService emailService,
                           @Value("${bpm.document.filename.format}") String pdfFileNameTemplate,
                           @Value("${bpm.project.fullDocumentCode.format}") String fullDocumentCodeTemplate,
                           ProjectRepository projectRepository,
                           EntityManager entityManager) {
        this.documentRepository = documentRepository;
        this.documentTypeService = documentTypeService;
        this.userService = userService;
        this.pathForPdfFont = pathForPdfFont;
        this.emailService = emailService;
        this.pdfFileNameTemplate = pdfFileNameTemplate;
        this.fullDocumentCodeTemplate = fullDocumentCodeTemplate;
        this.projectRepository = projectRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public void createDefaultDocument(ProjectEntity project, DocumentType type) {
        DocumentTypeEntity documentType = documentTypeService.getByType(type);
        DocumentEntity defaultDocument = DocumentEntity.builder()
                .project(project)
                .code(documentType.getCode())
                .numberInProject(documentType.getDefaultPageNumber())
                .documentType(documentType)
                .name(documentType.getName())
                .reassemblyRequired(true)
                .deleted(false)
                .contentFile(new FileEntity(documentType.getFirstPageFile().getFileInBytes()))
                .documentFile(new FileEntity())
                .build();
        DocumentEntity savedDocument = documentRepository.save(defaultDocument);
        reassembleDocument(savedDocument);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public DocumentEntity createEditableDocumentForSave(DocumentEntity document, MultipartFile file) {
        document.setId(null);
        document.setReassemblyRequired(true);
        document.setDeleted(false);
        updateBasicFieldsWithExistenceCheck(document);
        DocumentType currentDocType = document.getDocumentType().getType();
        if (GENERAL_INFORMATION.equals(currentDocType)) {
            projectRepository.setReassemblyRequiredById(document.getProject().getId(), true);
            setReassemblyRequiredByProjectId(document.getProject().getId(), true);
            DocumentEntity createdDocument = createGeneralInfo(document, file);
            emailService.sendEmailOnDocumentCreate(createdDocument);
            return createdDocument;
        }
        if (DRAWING.equals(currentDocType)) {
            projectRepository.setReassemblyRequiredById(document.getProject().getId(), true);
            setReassemblyRequiredByProjectId(document.getProject().getId(), true);
            DocumentEntity createdDocument = createDrawing(document, file);
            emailService.sendEmailOnDocumentCreate(createdDocument);
            return createdDocument;
        }
        throw new InvalidInputDataException("Can't create document. Wrong document type.");
    }

    @Transactional
    public DocumentEntity reassembleDocument(DocumentEntity document) {
        if (document.getReassemblyRequired()) {
            return switch (document.getDocumentType().getType()) {
                case COVER_PAGE -> reassembleCoverPage(document);
                case TITLE_PAGE -> reassembleTitlePage(document);
                case TABLE_OF_CONTENTS -> reassembleTableOfContents(document);
                case GENERAL_INFORMATION -> reassembleGeneralInformation(document);
                case DRAWING -> reassembleDrawing(document);
            };
        } else {
            return document;
        }
    }

    public List<DocumentEntity> getAllByProjectId(Long projectId) {
        List<DocumentEntity> documents = new ArrayList<>();
        if (projectId != null) {
            documents = documentRepository.findByProjectIdAndDeletedOrderByNumberInProject(projectId, false);
        }
        return documents;
    }

    public DocumentEntity getById(Long documentId) {
        return documentRepository.findById(documentId).orElseThrow(() -> {
            throw new NotFoundCustomApplicationException(String.format("Document with ID %d not found", documentId));
        });
    }

    public DocumentEntity getByIdAndProjectId(Long documentId, Long projectId) {
        return documentRepository.findByIdAndProjectId(documentId, projectId).orElseThrow(() -> {
            throw new NotFoundCustomApplicationException(String.format("Document with ID = %d not found for Project with ID = %d", documentId, projectId));
        });
    }

    public void getFileForDownload(Long documentId, Long projectId, HttpServletResponse response) {
        DocumentEntity document = getByIdAndProjectId(documentId, projectId);
        byte[] documentInPdf = document.getDocumentFile().getFileInBytes();
        String documentFileName = String.format(pdfFileNameTemplate, getFullCode(document), document.getName());
        getFile(response, documentInPdf, documentFileName);
    }

    @Transactional
    public DocumentEntity update(DocumentEntity documentForUpdate, MultipartFile file) {
        updateBasicFieldsWithExistenceCheck(documentForUpdate);
        DocumentEntity updatableDocument = getByIdAndProjectId(documentForUpdate.getId(), documentForUpdate.getProject().getId());
        DocumentType currentDocType = updatableDocument.getDocumentType().getType();
        if (file == null || file.isEmpty()) {
            if (GENERAL_INFORMATION.equals(currentDocType) || DRAWING.equals(currentDocType)) {
                DocumentEntity updatedDocument = getUpdatedDocument(documentForUpdate, updatableDocument, updatableDocument.getContentFile().getFileInBytes());
                emailService.sendEmailOnDocumentEdit(updatedDocument);
                return updatedDocument;
            }
        } else {
            try {
                byte[] uploadedFileInBytes = file.getBytes();
                String contentType = file.getContentType();
                if (contentType != null) {
                    if (GENERAL_INFORMATION.equals(currentDocType) && contentType.contains("text")) {
                        byte[] textDocumentContentInPdf = new PdfDocumentGenerator(updatableDocument.getDocumentType().getFirstPageFile().getFileInBytes(), pathForPdfFont)
                                .getFilledTextDocument(uploadedFileInBytes, updatableDocument.getDocumentType().getGeneralPageFile().getFileInBytes())
                                .getPdfDocumentInBytes();
                        DocumentEntity updatedDocument = getUpdatedDocument(documentForUpdate, updatableDocument, textDocumentContentInPdf);
                        emailService.sendEmailOnDocumentEdit(updatedDocument);
                        return updatedDocument;
                    }
                    if (DRAWING.equals(currentDocType) && contentType.contains("pdf")) {
                        DocumentEntity updatedDocument = getUpdatedDocument(documentForUpdate, updatableDocument, uploadedFileInBytes);
                        emailService.sendEmailOnDocumentEdit(updatedDocument);
                        return updatedDocument;
                    }
                }
            } catch (IOException e) {
                log.debug(e.getMessage(), e);
                throw new InvalidInputDataException(String.format("Can't update document with id = %d. Broken content file", documentForUpdate.getId()), e);
            }
        }
        throw new InvalidInputDataException(String.format("Can't update document with id = %d. Wrong document type or file type", documentForUpdate.getId()));
    }

    public String getFullCode(DocumentEntity document) {
        return String.format(fullDocumentCodeTemplate,
                document.getProject().getCode(),
                document.getProject().getVolumeNumber(),
                document.getCode(),
                document.getNumberInProject(),
                document.getProject().getStage().getCode());
    }

    @Transactional
    public void setDeleted(Long projectId, Long documentId, Boolean deleted) {
        DocumentEntity documentForDelete = getByIdAndProjectId(documentId, projectId);
        if (!documentForDelete.getDocumentType().getUnmodified()) {
            documentForDelete.setDeleted(deleted);
            documentRepository.save(documentForDelete);
            projectRepository.setReassemblyRequiredById(projectId, true);
        } else
            throw new InvalidInputDataException(String.format("Can't delete or restore Document with ID = %d. There is unmodified documentType.", documentId));
    }

    @Transactional
    public void setReassemblyRequiredByProjectId(Long projectId, boolean reassemblyRequired) {
        List<DocumentEntity> documents = getAllByProjectId(projectId);
        documents.stream().peek(d -> d.setReassemblyRequired(reassemblyRequired)).forEach(documentRepository::save);
    }

    private DocumentEntity reassembleCoverPage(DocumentEntity document) {
        byte[] contentInPdf = document.getContentFile().getFileInBytes();
        byte[] coverPageInPdf = new PdfDocumentGenerator(contentInPdf, pathForPdfFont)
                .getFilledA4CoverDocument(getDocumentDataForPdf(document, getFullCode(document)))
                .getPdfDocumentInBytes();
        document.getDocumentFile().setFileInBytes(coverPageInPdf);
        document.setReassemblyRequired(false);
        return documentRepository.save(document);
    }

    private DocumentEntity getUpdatedDocument(DocumentEntity documentForUpdate, DocumentEntity updatableDocument, byte[] contentDocumentForUpdate) {
        updatableDocument.setName(Optional.ofNullable(documentForUpdate.getName()).orElse(updatableDocument.getName()));
        updatableDocument.setCode(Optional.ofNullable(documentForUpdate.getCode()).orElse(updatableDocument.getCode()));
        updatableDocument.setDesigner(Optional.ofNullable(documentForUpdate.getDesigner()).orElse(updatableDocument.getDesigner()));
        updatableDocument.setSupervisor(Optional.ofNullable(documentForUpdate.getSupervisor()).orElse(updatableDocument.getSupervisor()));
        updatableDocument.setReassemblyRequired(true);
        projectRepository.setReassemblyRequiredById(updatableDocument.getProject().getId(), true);
        updatableDocument.getContentFile().setFileInBytes(contentDocumentForUpdate);
        updatableDocument.setVersion(documentForUpdate.getVersion());
        entityManager.detach(updatableDocument);
        return documentRepository.save(updatableDocument);
    }

    private DocumentEntity reassembleTitlePage(DocumentEntity document) {
        byte[] contentInPdf = document.getContentFile().getFileInBytes();
        byte[] titlePageInPdf = new PdfDocumentGenerator(contentInPdf, pathForPdfFont)
                .getFilledA4TitleListDocument(getDocumentDataForPdf(document, getFullCode(document)))
                .getPdfDocumentInBytes();
        document.getDocumentFile().setFileInBytes(titlePageInPdf);
        document.setReassemblyRequired(false);
        return documentRepository.save(document);
    }

    private DocumentEntity reassembleTableOfContents(DocumentEntity document) {
        byte[] firstPageTemplate = document.getDocumentType().getFirstPageFile().getFileInBytes();
        byte[] generalPageTemplate = document.getDocumentType().getGeneralPageFile().getFileInBytes();
        List<DocumentEntity> documentEntities = getAllByProjectId(document.getProject().getId());
        List<RowOfContentsDocument> rows = documentEntities.stream()
                .filter(x -> !COVER_PAGE.equals(x.getDocumentType().getType()))
                .map(x -> RowOfContentsDocument.builder()
                        .column1(getFullCode(x))
                        .column2(x.getName())
                        .column3("")
                        .build())
                .toList();
        byte[] tableOfContentsInPdf = new PdfDocumentGenerator(firstPageTemplate, pathForPdfFont)
                .getFilledContentsDocument(rows, generalPageTemplate)
                .getFilledTextDocumentTitleBlock(getDocumentDataForPdf(document, getFullCode(document)))
                .getPdfDocumentInBytes();
        document.getDocumentFile().setFileInBytes(tableOfContentsInPdf);
        document.setReassemblyRequired(false);
        return documentRepository.save(document);
    }

    private DocumentEntity reassembleGeneralInformation(DocumentEntity document) {
        byte[] contentInPdf = document.getContentFile().getFileInBytes();
        byte[] filledTextDocumentInPdf = new PdfDocumentGenerator(contentInPdf, pathForPdfFont)
                .getFilledTextDocumentTitleBlock(getDocumentDataForPdf(document, getFullCode(document)))
                .getPdfDocumentInBytes();
        document.getDocumentFile().setFileInBytes(filledTextDocumentInPdf);
        document.setReassemblyRequired(false);
        return documentRepository.save(document);
    }

    private DocumentEntity reassembleDrawing(DocumentEntity document) {
        byte[] contentInPdf = document.getContentFile().getFileInBytes();
        byte[] filledDrawingDocumentInPdf = new PdfDocumentGenerator(contentInPdf, pathForPdfFont)
                .getFilledBlueprintDocumentTitleBlock(getDocumentDataForPdf(document, getFullCode(document)))
                .getPdfDocumentInBytes();
        document.getDocumentFile().setFileInBytes(filledDrawingDocumentInPdf);
        document.setReassemblyRequired(false);
        return documentRepository.save(document);
    }

    private DocumentEntity createGeneralInfo(DocumentEntity document, MultipartFile textFile) {
        byte[] firstPageTemplate = document.getDocumentType().getFirstPageFile().getFileInBytes();
        byte[] generalPageTemplate = document.getDocumentType().getGeneralPageFile().getFileInBytes();
        if (textFile == null || textFile.isEmpty()) {
            document.setContentFile(new FileEntity(firstPageTemplate));
        } else {
            if (textFile.getContentType() != null && textFile.getContentType().contains("text")) {
                try {
                    byte[] textInBytes = textFile.getBytes();
                    byte[] textDocument = new PdfDocumentGenerator(firstPageTemplate, pathForPdfFont)
                            .getFilledTextDocument(textInBytes, generalPageTemplate)
                            .getPdfDocumentInBytes();
                    document.setContentFile(new FileEntity(textDocument));
                } catch (IOException e) {
                    log.debug(e.getMessage(), e);
                    throw new InvalidInputDataException("Can't create text document, broken file ", e);
                }
            } else {
                throw new InvalidInputDataException("Can't create text document. Wrong file type");
            }

        }
        return reassembleDocument(documentRepository.save(document));
    }

    private DocumentEntity createDrawing(DocumentEntity document, MultipartFile drawingFile) {
        if (drawingFile == null || drawingFile.isEmpty()) {
            document.setContentFile(new FileEntity(document.getDocumentType().getFirstPageFile().getFileInBytes()));
        } else {
            if (drawingFile.getContentType() != null && drawingFile.getContentType().contains("pdf")) {
                try {
                    document.setContentFile(new FileEntity(drawingFile.getBytes()));
                } catch (IOException e) {
                    log.debug(e.getMessage(), e);
                    throw new InvalidInputDataException("Can't create drawing document, broken file ", e);
                }
            } else {
                throw new InvalidInputDataException("Can't create drawing document. Wrong file type");
            }
        }
        return reassembleDocument(documentRepository.save(document));
    }

    private void updateBasicFieldsWithExistenceCheck(DocumentEntity document) {
        document.setDocumentType(documentTypeService.getById(document.getDocumentType().getId()));
        document.setSupervisor(Optional.ofNullable(document.getSupervisor())
                .map(u -> userService.getById(u.getId()))
                .orElse(null));
        document.setDesigner(Optional.ofNullable(document.getSupervisor())
                .map(u -> userService.getById(u.getId()))
                .orElse(null));
    }
}
