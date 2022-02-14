package com.dataart.blueprintsmanager.service;

import com.dataart.blueprintsmanager.dto.DocumentDto;
import com.dataart.blueprintsmanager.exceptions.CustomApplicationException;
import com.dataart.blueprintsmanager.pdf.CompanyDataForPdf;
import com.dataart.blueprintsmanager.pdf.DocumentDataForPdf;
import com.dataart.blueprintsmanager.pdf.PdfDocumentGenerator;
import com.dataart.blueprintsmanager.pdf.RowOfContentsDocument;
import com.dataart.blueprintsmanager.persistence.entity.DocumentEntity;
import com.dataart.blueprintsmanager.persistence.entity.DocumentType;
import com.dataart.blueprintsmanager.persistence.entity.UserEntity;
import com.dataart.blueprintsmanager.persistence.repository.DocumentRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class DocumentService {
    private DocumentRepository documentRepository;
    private DocumentTypeService documentTypeService;

    public void createCoverPage(Long projectId) {
        DocumentDto document = DocumentDto.builder()
                .projectId(projectId)
                .code("О")
                .numberInProject(1)
                .typeId(DocumentType.COVER_PAGE.getId())
                .name("Обложка")
                .reassemblyRequired(true)
                .editTime(LocalDateTime.now())
                .build();
        byte[] coverPageTemplate = documentTypeService.getDocumentTemplateByTypeId(document.getTypeId()).get(0);
        reassembleDocument(save(document, coverPageTemplate).getId());
    }

    public void createTitlePage(Long projectId) {
        DocumentDto document = DocumentDto.builder()
                .projectId(projectId)
                .code("ТЛ")
                .numberInProject(2)
                .typeId(DocumentType.TITLE_PAGE.getId())
                .name("Титульный лист")
                .reassemblyRequired(true)
                .editTime(LocalDateTime.now())
                .build();
        byte[] titleListTemplate = documentTypeService.getDocumentTemplateByTypeId(document.getTypeId()).get(0);
        reassembleDocument(save(document, titleListTemplate).getId());
    }

    public void createTableOfContents(Long projectId) {
        DocumentDto document = DocumentDto.builder()
                .projectId(projectId)
                .code("СТ")
                .numberInProject(3)
                .typeId(DocumentType.TABLE_OF_CONTENTS.getId())
                .name("Состав тома")
                .reassemblyRequired(true)
                .editTime(LocalDateTime.now())
                .build();
        byte[] titleListTemplate = documentTypeService.getDocumentTemplateByTypeId(document.getTypeId()).get(0);
        reassembleDocument(save(document, titleListTemplate).getId());
    }

    public DocumentDto save(DocumentDto documentForSave, byte[] documentTemplate) {
        DocumentEntity documentEntity = documentForSave.updateEntity(getEmpty());
        documentEntity.setContentInPdf(documentTemplate);
        return new DocumentDto(documentRepository.createTransactional(documentEntity));
    }

    public DocumentDto reassembleDocument(Long documentId) {
        DocumentEntity document = documentRepository.fetchByIdTransactional(documentId);
        if (document.getReassemblyRequired()) {
            if (document.getDocumentType().equals(DocumentType.COVER_PAGE)) {
                return new DocumentDto(reassembleCoverPage(document));
            }
            if (document.getDocumentType().equals(DocumentType.TITLE_PAGE)) {
                return new DocumentDto(reassembleTitlePage(document));
            }
            if (document.getDocumentType().equals(DocumentType.TABLE_OF_CONTENTS)) {
                return new DocumentDto(reassembleTableOfContents(document));
            }
            if (document.getDocumentType().equals(DocumentType.GENERAL_INFORMATION)) {
                return new DocumentDto(reassembleGeneralInformation(document));
            }
            if (document.getDocumentType().equals(DocumentType.DRAWING)) {
                return new DocumentDto(reassembleDrawing(document));
            }
            String message = String.format("Wrong type of document %s", document.getDocumentType());
            log.debug(message);
            throw new CustomApplicationException(message);
        }
        return getById(documentId);
    }

    private DocumentEntity reassembleCoverPage(DocumentEntity document) {
        byte[] contentInPdf = documentRepository.fetchContentInPdfByDocumentId(document.getId());
        PdfDocumentGenerator coverPageGenerator = new PdfDocumentGenerator(contentInPdf);
        byte[] coverPageInPdf = coverPageGenerator.getFilledA4CoverDocument(getDocumentDataForPdf(document)).getPdfDocumentInBytes();
        return documentRepository.updateDocumentInPdfTransactional(document.getId(), coverPageInPdf);
    }

    private DocumentEntity reassembleTitlePage(DocumentEntity document) {
        byte[] contentInPdf = documentRepository.fetchContentInPdfByDocumentId(document.getId());
        PdfDocumentGenerator coverPageGenerator = new PdfDocumentGenerator(contentInPdf);
        byte[] titlePageInPdf = coverPageGenerator.getFilledA4TitleListDocument(getDocumentDataForPdf(document)).getPdfDocumentInBytes();
        return documentRepository.updateDocumentInPdfTransactional(document.getId(), titlePageInPdf);
    }

    private DocumentEntity reassembleTableOfContents(DocumentEntity document) {
        byte[] firstPageTemplate = documentTypeService.getDocumentTemplateByTypeId(document.getDocumentType().getId()).get(0);
        byte[] generalPageTemplate = documentTypeService.getDocumentTemplateByTypeId(document.getDocumentType().getId()).get(1);
        List<DocumentEntity> documentEntities = documentRepository.fetchAllByProjectIdTransactional(document.getProject().getId());
        List<RowOfContentsDocument> rows = documentEntities.stream().filter(x -> !DocumentType.COVER_PAGE.equals(x.getDocumentType()))
                .map(x -> RowOfContentsDocument.builder()
                        .column1(getFullCode(x))
                        .column2(x.getName())
                        .column3("")
                        .build())
                .toList();
        PdfDocumentGenerator tableOfContentsGenerator = new PdfDocumentGenerator(firstPageTemplate);
        byte[] tableOfContentsInPdf = tableOfContentsGenerator.getFilledContentsDocument(rows, generalPageTemplate).getFilledTextDocumentTitleBlock(getDocumentDataForPdf(document)).getPdfDocumentInBytes();
        documentRepository.updateContentInPdfTransactional(document.getId(), tableOfContentsInPdf);
        return documentRepository.updateDocumentInPdfTransactional(document.getId(), tableOfContentsInPdf);
    }

    private DocumentEntity reassembleGeneralInformation(DocumentEntity document) {
        return new DocumentEntity();
    }

    private DocumentEntity reassembleDrawing(DocumentEntity document) {
        return new DocumentEntity();
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
                .releaseDate(document.getProject().getReleaseDate().format(DateTimeFormatter.ofPattern("MM.yy")))
                .company(companyDataForPdf)
                .volumeNumber(document.getProject().getVolumeNumber())
                .volumeName(document.getProject().getVolumeName())
                .releaseDateForCover(document.getProject().getReleaseDate().format(DateTimeFormatter.ofPattern("yyyy")))
                .codeForCover(document.getProject().getCode())
                .stageForCover(document.getProject().getStage().getName())
                .build();
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
                map(d -> {
                    DocumentDto documentDto = new DocumentDto(d);
                    documentDto.setDocumentFullCode(getFullCode(d));
                    return documentDto;
                }).
                collect(Collectors.toList());
    }

    public DocumentDto getById(Long documentId) {
        DocumentEntity documentEntity = documentRepository.fetchByIdTransactional(documentId);
        DocumentDto documentDto = new DocumentDto(documentEntity);
        documentDto.setDocumentFullCode(getFullCode(documentEntity));
        return documentDto;
    }

    private DocumentEntity getEmpty() {
        return DocumentEntity.builder()
                .id(null)
                .numberInProject(0)
                .documentType(DocumentType.DRAWING)
                .name("Новый документ")
                .code("NEW")
                .designer(new UserEntity())
                .supervisor(new UserEntity())
                .reassemblyRequired(true)
                .editTime(LocalDateTime.now()).build();
    }

    public DocumentDto getFileForDownload(Long documentId) {
        DocumentEntity document = documentRepository.fetchByIdTransactional(documentId);
        byte[] documentInPdf = documentRepository.fetchDocumentInPdfByDocumentId(documentId);
        StringBuilder documentFileName = new StringBuilder();
        documentFileName
                .append(getFullCode(document))
                .append("_")
                .append(document.getName())
                .append(".pdf");
        return DocumentDto.builder()
                .documentFileName(documentFileName.toString())
                .documentInPdf(documentInPdf).build();
    }

    private String getFullCode(DocumentEntity document) {
        StringBuilder fullCode = new StringBuilder();
        return fullCode
                .append(document.getProject().getCode())
                .append("-")
                .append(document.getProject().getVolumeNumber())
                .append("-")
                .append(document.getCode())
                .append("-")
                .append(document.getNumberInProject())
                .append("-")
                .append(document.getProject().getStage().getCode())
                .toString();
    }

    public byte[] getInPdfById(Long id) {
        return documentRepository.fetchDocumentInPdfByDocumentId(id);
    }
}
