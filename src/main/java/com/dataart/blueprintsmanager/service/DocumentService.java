package com.dataart.blueprintsmanager.service;

import com.dataart.blueprintsmanager.dto.DocumentDto;
import com.dataart.blueprintsmanager.email.EmailService;
import com.dataart.blueprintsmanager.exceptions.EditDocumentException;
import com.dataart.blueprintsmanager.pdf.CompanyDataForPdf;
import com.dataart.blueprintsmanager.pdf.DocumentDataForPdf;
import com.dataart.blueprintsmanager.pdf.PdfDocumentGenerator;
import com.dataart.blueprintsmanager.pdf.RowOfContentsDocument;
import com.dataart.blueprintsmanager.persistence.entity.DocumentEntity;
import com.dataart.blueprintsmanager.persistence.entity.DocumentType;
import com.dataart.blueprintsmanager.persistence.entity.ProjectEntity;
import com.dataart.blueprintsmanager.persistence.entity.UserEntity;
import com.dataart.blueprintsmanager.persistence.repository.DocumentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.dataart.blueprintsmanager.util.ApplicationUtil.getFile;

@Service
@Slf4j
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final DocumentTypeService documentTypeService;
    private final UserService userService;
    private final String pathForPdfFont;
    private static final DateTimeFormatter releaseDateFormat = DateTimeFormatter.ofPattern("MM.yy");
    private static final DateTimeFormatter releaseDateForCoverFormat = DateTimeFormatter.ofPattern("yyyy");
    private final EmailService emailService;
    private final String pdfFileNameTemplate;
    private final String defaultDocumentName;
    private final String defaultDocumentCode;

    public DocumentService(DocumentRepository documentRepository, DocumentTypeService documentTypeService,
                           UserService userService,
                           @Value("${bpm.pdf.fontfilepath}") String pathForPdfFont,
                           EmailService emailService,
                           @Value("${bpm.document.filename.format}") String pdfFileNameTemplate,
                           @Value("${bpm.document.default.name}") String defaultDocumentName,
                           @Value("${bpm.document.default.code}") String defaultDocumentCode) {
        this.documentRepository = documentRepository;
        this.documentTypeService = documentTypeService;
        this.userService = userService;
        this.pathForPdfFont = pathForPdfFont;
        this.emailService = emailService;
        this.pdfFileNameTemplate = pdfFileNameTemplate;
        this.defaultDocumentName = defaultDocumentName;
        this.defaultDocumentCode = defaultDocumentCode;
    }


    public void createCoverPage(Long projectId) {
        DocumentDto document = DocumentDto.builder()
                .projectId(projectId)
                .code("О")
                .numberInProject(1)
                .type(DocumentType.COVER_PAGE)
                .name("Обложка")
                .reassemblyRequired(true)
                .editTime(LocalDateTime.now())
                .build();
        byte[] coverPageTemplate = documentTypeService.getFirstPageTemplateByTypeId(document.getType().getId());
        reassembleDocument(save(document, coverPageTemplate).getId());
    }

    public void createTitlePage(Long projectId) {
        DocumentDto document = DocumentDto.builder()
                .projectId(projectId)
                .code("ТЛ")
                .numberInProject(2)
                .type(DocumentType.TITLE_PAGE)
                .name("Титульный лист")
                .reassemblyRequired(true)
                .editTime(LocalDateTime.now())
                .build();
        byte[] titleListTemplate = documentTypeService.getFirstPageTemplateByTypeId(document.getType().getId());
        reassembleDocument(save(document, titleListTemplate).getId());
    }

    public void createTableOfContents(Long projectId) {
        DocumentDto document = DocumentDto.builder()
                .projectId(projectId)
                .code("СТ")
                .numberInProject(3)
                .type(DocumentType.TABLE_OF_CONTENTS)
                .name("Состав тома")
                .reassemblyRequired(true)
                .editTime(LocalDateTime.now())
                .build();
        byte[] titleListTemplate = documentTypeService.getFirstPageTemplateByTypeId(document.getType().getId());
        reassembleDocument(save(document, titleListTemplate).getId());
    }

    public DocumentDto createEditableDocumentForSave(DocumentDto document, MultipartFile file) {
        DocumentType currentDocType = document.getType();
        if (currentDocType == null) {
            throw new EditDocumentException("Can't create document. Wrong document type or file type");
        }
        document.setEditTime(LocalDateTime.now());
        if (file.isEmpty()) {
            if (DocumentType.GENERAL_INFORMATION.equals(currentDocType)) {
                DocumentDto createdDocumentDto = createGeneralInfo(document, null);
                emailService.sendEmailOnDocumentCreate(documentRepository.fetchById(createdDocumentDto.getId()));
                return createdDocumentDto;
            }
            if (DocumentType.DRAWING.equals(currentDocType)) {
                DocumentDto createdDocumentDto = createDrawing(document, null);
                emailService.sendEmailOnDocumentCreate(documentRepository.fetchById(createdDocumentDto.getId()));
                return createdDocumentDto;
            }
        }
        try {
            byte[] uploadedFileInBytes = file.getBytes();
            String contentType = file.getContentType();
            if (contentType != null) {
                if (DocumentType.GENERAL_INFORMATION.equals(currentDocType) && contentType.contains("text")) {
                    DocumentDto createdDocumentDto = createGeneralInfo(document, uploadedFileInBytes);
                    emailService.sendEmailOnDocumentCreate(documentRepository.fetchById(createdDocumentDto.getId()));
                    return createdDocumentDto;
                }
                if (DocumentType.DRAWING.equals(currentDocType) && contentType.contains("pdf")) {
                    DocumentDto createdDocumentDto = createDrawing(document, uploadedFileInBytes);
                    emailService.sendEmailOnDocumentCreate(documentRepository.fetchById(createdDocumentDto.getId()));
                    return createdDocumentDto;
                }
            }
            throw new EditDocumentException("Can't create document. Wrong document type or file type");
        } catch (IOException e) {
            log.debug(e.getMessage(), e);
            throw new EditDocumentException("Can't create document, broken file ", e);
        }
    }

    public DocumentDto reassembleDocument(Long documentId) {
        DocumentEntity document = documentRepository.fetchById(documentId);
        if (document.getReassemblyRequired() && document.getDocumentType() != null) {
            return switch (document.getDocumentType()) {
                case COVER_PAGE -> new DocumentDto(reassembleCoverPage(document));
                case TITLE_PAGE -> new DocumentDto(reassembleTitlePage(document));
                case TABLE_OF_CONTENTS -> new DocumentDto(reassembleTableOfContents(document));
                case GENERAL_INFORMATION -> new DocumentDto(reassembleGeneralInformation(document));
                case DRAWING -> new DocumentDto(reassembleDrawing(document));
            };
        }
        return getById(documentId);
    }

    public List<DocumentDto> getAllByProjectId(Long projectId) {
        List<DocumentEntity> documents = new ArrayList<>();
        if (projectId != null) {
            documents = documentRepository.fetchAllByProjectId(projectId);
        }
        return documents.stream().
                filter(Objects::nonNull).
                map(d -> {
                    DocumentDto documentDto = new DocumentDto(d);
                    documentDto.setDocumentFullCode(getFullCode(d));
                    return documentDto;
                }).
                collect(Collectors.toList());
    }

    public DocumentDto getById(Long documentId) {
        DocumentEntity documentEntity = documentRepository.fetchById(documentId);
        DocumentDto documentDto = new DocumentDto(documentEntity);
        documentDto.setDocumentFullCode(getFullCode(documentEntity));
        return documentDto;
    }

    public void getFileForDownload(Long documentId, HttpServletResponse response) {
        DocumentEntity document = documentRepository.fetchById(documentId);
        byte[] documentInPdf = documentRepository.fetchDocumentInPdfByDocumentId(documentId);
        String documentFileName = String.format(pdfFileNameTemplate, getFullCode(document), document.getName());
        getFile(response, documentInPdf, documentFileName);
    }

    public byte[] getInPdfById(Long id) {
        return documentRepository.fetchDocumentInPdfByDocumentId(id);
    }

    public DocumentDto getNew(Long projectId) {
        DocumentDto newDocument = new DocumentDto(getEmpty());
        newDocument.setProjectId(projectId);
        newDocument.setType(null);
        return newDocument;
    }

    public DocumentDto update(DocumentDto documentForUpdate, MultipartFile file) {
        DocumentEntity updatableDocument = documentRepository.fetchById(documentForUpdate.getId());
        DocumentType currentDocType = updatableDocument.getDocumentType();
        documentForUpdate.setEditTime(LocalDateTime.now());
        if (file == null || file.isEmpty()) {
            if (DocumentType.GENERAL_INFORMATION.equals(currentDocType) || DocumentType.DRAWING.equals(currentDocType)) {
                DocumentDto updatedDocument = getUpdatedDocument(documentForUpdate, updatableDocument, null);
                emailService.sendEmailOnDocumentEdit(documentRepository.fetchById(updatedDocument.getId()));
                return updatedDocument;
            }
        }
        try {
            byte[] uploadedFileInBytes = file.getInputStream().readAllBytes();
            String contentType = file.getContentType();
            if (contentType != null) {
                if (DocumentType.GENERAL_INFORMATION.equals(currentDocType) && contentType.contains("text")) {
                    byte[] textDocumentContentInPdf = new PdfDocumentGenerator(documentTypeService.getFirstPageTemplateByTypeId(currentDocType.getId()), pathForPdfFont)
                            .getFilledTextDocument(uploadedFileInBytes, documentTypeService.getGeneralPageTemplateByTypeId(currentDocType.getId()))
                            .getPdfDocumentInBytes();
                    DocumentDto updatedDocument = getUpdatedDocument(documentForUpdate, updatableDocument, textDocumentContentInPdf);
                    emailService.sendEmailOnDocumentEdit(documentRepository.fetchById(updatedDocument.getId()));
                    return updatedDocument;
                }
                if (DocumentType.DRAWING.equals(currentDocType) && contentType.contains("pdf")) {
                    DocumentDto updatedDocument = getUpdatedDocument(documentForUpdate, updatableDocument, uploadedFileInBytes);
                    emailService.sendEmailOnDocumentEdit(documentRepository.fetchById(updatedDocument.getId()));
                    return updatedDocument;
                }
            }
            throw new EditDocumentException(String.format("Can't update document with id = %d. Wrong document type or file type", documentForUpdate.getId()));
        } catch (IOException e) {
            log.debug(e.getMessage(), e);
            throw new EditDocumentException(String.format("Can't update document with id = %d. Broken content file", documentForUpdate.getId()), e);
        }
    }

    private DocumentDto getUpdatedDocument(DocumentDto documentForUpdate, DocumentEntity updatableDocument, byte[] contentDocumentForUpdate) {
        updatableDocument.setName(Optional.ofNullable(documentForUpdate.getName()).orElse(updatableDocument.getName()));
        updatableDocument.setCode(Optional.ofNullable(documentForUpdate.getCode()).orElse(updatableDocument.getCode()));
        updatableDocument.setDesigner(Optional.ofNullable(documentForUpdate.getDesignerId()).map(userService::getById).orElse(updatableDocument.getDesigner()));
        updatableDocument.setSupervisor(Optional.ofNullable(documentForUpdate.getSupervisorId()).map(userService::getById).orElse(updatableDocument.getSupervisor()));
        updatableDocument.setReassemblyRequired(true);
        updatableDocument.setEditTime(LocalDateTime.now());
        updatableDocument.setContentInPdf(contentDocumentForUpdate);
        return new DocumentDto(documentRepository.updateTransactional(updatableDocument));
    }

    public Long deleteById(Long documentId) {
        return documentRepository.deleteByIdTransactional(documentId);
    }

    private DocumentDto save(DocumentDto documentForSave, byte[] documentTemplate) {
        DocumentEntity documentEntityForSave = DocumentEntity.builder()
                .project(ProjectEntity.builder().id(documentForSave.getProjectId()).build())
                .documentType(documentForSave.getType())
                .name(documentForSave.getName())
                .code(documentForSave.getCode())
                .designer(Optional.ofNullable(documentForSave.getDesignerId())
                        .map(userService::getById)
                        .orElse(UserEntity.builder().id(null).build()))
                .supervisor(Optional.ofNullable(documentForSave.getSupervisorId())
                        .map(userService::getById)
                        .orElse(UserEntity.builder().id(null).build()))
                .reassemblyRequired(true)
                .editTime(LocalDateTime.now())
                .build();
        documentEntityForSave.setContentInPdf(documentTemplate);
        return new DocumentDto(documentRepository.createTransactional(documentEntityForSave));
    }

    private DocumentEntity reassembleCoverPage(DocumentEntity document) {
        byte[] contentInPdf = documentRepository.fetchContentInPdfByDocumentId(document.getId());
        byte[] coverPageInPdf = new PdfDocumentGenerator(contentInPdf, pathForPdfFont)
                .getFilledA4CoverDocument(getDocumentDataForPdf(document))
                .getPdfDocumentInBytes();
        return documentRepository.updateDocumentInPdfByDocumentIdTransactional(document.getId(), coverPageInPdf);
    }

    private DocumentEntity reassembleTitlePage(DocumentEntity document) {
        byte[] contentInPdf = documentRepository.fetchContentInPdfByDocumentId(document.getId());
        byte[] titlePageInPdf = new PdfDocumentGenerator(contentInPdf, pathForPdfFont)
                .getFilledA4TitleListDocument(getDocumentDataForPdf(document))
                .getPdfDocumentInBytes();
        return documentRepository.updateDocumentInPdfByDocumentIdTransactional(document.getId(), titlePageInPdf);
    }

    private DocumentEntity reassembleTableOfContents(DocumentEntity document) {
        byte[] firstPageTemplate = documentTypeService.getFirstPageTemplateByTypeId(document.getDocumentType().getId());
        byte[] generalPageTemplate = documentTypeService.getGeneralPageTemplateByTypeId(document.getDocumentType().getId());
        List<DocumentEntity> documentEntities = documentRepository.fetchAllByProjectId(document.getProject().getId());
        List<RowOfContentsDocument> rows = documentEntities.stream().filter(x -> !DocumentType.COVER_PAGE.equals(x.getDocumentType()))
                .map(x -> RowOfContentsDocument.builder()
                        .column1(getFullCode(x))
                        .column2(x.getName())
                        .column3("")
                        .build())
                .toList();
        byte[] tableOfContentsInPdf = new PdfDocumentGenerator(firstPageTemplate, pathForPdfFont)
                .getFilledContentsDocument(rows, generalPageTemplate)
                .getFilledTextDocumentTitleBlock(getDocumentDataForPdf(document))
                .getPdfDocumentInBytes();
        documentRepository.updateContentInPdfByDocumentId(document.getId(), tableOfContentsInPdf);
        return documentRepository.updateDocumentInPdfByDocumentIdTransactional(document.getId(), tableOfContentsInPdf);
    }

    private DocumentEntity reassembleGeneralInformation(DocumentEntity document) {
        byte[] contentInPdf = documentRepository.fetchContentInPdfByDocumentId(document.getId());
        byte[] filledTextDocumentInPdf = new PdfDocumentGenerator(contentInPdf, pathForPdfFont)
                .getFilledTextDocumentTitleBlock(getDocumentDataForPdf(document))
                .getPdfDocumentInBytes();
        return documentRepository.updateDocumentInPdfByDocumentIdTransactional(document.getId(), filledTextDocumentInPdf);
    }

    private DocumentEntity reassembleDrawing(DocumentEntity document) {
        byte[] contentInPdf = documentRepository.fetchContentInPdfByDocumentId(document.getId());
        byte[] filledDrawingDocumentInPdf = new PdfDocumentGenerator(contentInPdf, pathForPdfFont)
                .getFilledBlueprintDocumentTitleBlock(getDocumentDataForPdf(document))
                .getPdfDocumentInBytes();
        return documentRepository.updateDocumentInPdfByDocumentIdTransactional(document.getId(), filledDrawingDocumentInPdf);
    }

    private DocumentDataForPdf getDocumentDataForPdf(DocumentEntity document) {
        CompanyDataForPdf companyDataForPdf = CompanyDataForPdf.builder()
                .city(document.getProject().getCompany().getCity())
                .logo(document.getProject().getCompany().getLogo())
                .signerName(document.getProject().getCompany().getSignerName())
                .signerPosition(document.getProject().getCompany().getSignerPosition())
                .name(document.getProject().getCompany().getName())
                .build();
        return DocumentDataForPdf.builder()
                .documentCode(getFullCode(document))
                .documentName(document.getName())
                .designerName(Optional.ofNullable(document.getDesigner())
                        .map(UserEntity::getLastName)
                        .orElse(Optional.ofNullable(document.getProject().getDesigner())
                                .map(UserEntity::getLastName)
                                .orElse("")))
                .designerSign(Optional.ofNullable(document.getDesigner())
                        .map(UserEntity::getSignature)
                        .orElse(Optional.ofNullable(document.getProject().getDesigner())
                                .map(UserEntity::getSignature)
                                .orElse(null)))
                .supervisorName(Optional.ofNullable(document.getSupervisor())
                        .map(UserEntity::getLastName)
                        .orElse(Optional.ofNullable(document.getProject().getSupervisor())
                                .map(UserEntity::getLastName)
                                .orElse("")))
                .supervisorSign(Optional.ofNullable(document.getSupervisor())
                        .map(UserEntity::getSignature)
                        .orElse(Optional.ofNullable(document.getProject().getSupervisor())
                                .map(UserEntity::getSignature)
                                .orElse(null)))
                .controllerName(Optional.ofNullable(document.getProject().getController())
                        .map(UserEntity::getLastName)
                        .orElse(null))
                .controllerSign(Optional.ofNullable(document.getProject().getController())
                        .map(UserEntity::getSignature)
                        .orElse(null))
                .chiefEngineerName(Optional.ofNullable(document.getProject().getChief())
                        .map(UserEntity::getLastName)
                        .orElse(null))
                .chiefEngineerSign(Optional.ofNullable(document.getProject().getChief())
                        .map(UserEntity::getSignature)
                        .orElse(null))
                .stage(document.getProject().getStage().getCode())
                .projectName(document.getProject().getName())
                .objectAddress(document.getProject().getObjectAddress())
                .releaseDate(document.getProject().getReleaseDate().format(releaseDateFormat))
                .company(companyDataForPdf)
                .volumeNumber(document.getProject().getVolumeNumber())
                .volumeName(document.getProject().getVolumeName())
                .releaseDateForCover(document.getProject().getReleaseDate().format(releaseDateForCoverFormat))
                .codeForCover(document.getProject().getCode())
                .stageForCover(document.getProject().getStage().getName())
                .build();
    }

    private DocumentEntity getEmpty() {
        return DocumentEntity.builder()
                .id(null)
                .numberInProject(0)
                .documentType(DocumentType.DRAWING)
                .name(defaultDocumentName)
                .code(defaultDocumentCode)
                .designer(new UserEntity())
                .supervisor(new UserEntity())
                .reassemblyRequired(true)
                .editTime(LocalDateTime.now()).build();
    }

    private String getFullCode(DocumentEntity document) {
        return String.format("%s-%d-%s-%d-%s",
                document.getProject().getCode(),
                document.getProject().getVolumeNumber(),
                document.getCode(),
                document.getNumberInProject(),
                document.getProject().getStage().getCode());
    }

    private DocumentDto createGeneralInfo(DocumentDto document, byte[] text) {
        byte[] firstPageTemplate = documentTypeService.getFirstPageTemplateByTypeId(document.getType().getId());
        byte[] generalPageTemplate = documentTypeService.getGeneralPageTemplateByTypeId(document.getType().getId());
        if (text == null || text.length == 0) {
            return reassembleDocument(save(document, firstPageTemplate).getId());
        }
        byte[] textDocument = new PdfDocumentGenerator(firstPageTemplate, pathForPdfFont).getFilledTextDocument(text, generalPageTemplate).getPdfDocumentInBytes();
        return reassembleDocument(save(document, textDocument).getId());
    }

    private DocumentDto createDrawing(DocumentDto document, byte[] drawing) {
        if (drawing == null || drawing.length == 0) {
            drawing = documentTypeService.getFirstPageTemplateByTypeId(document.getType().getId());
        }
        return reassembleDocument(save(document, drawing).getId());
    }
}
