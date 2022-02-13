package com.dataart.blueprintsmanager.service;

import com.dataart.blueprintsmanager.dto.DocumentDto;
import com.dataart.blueprintsmanager.pdf.CompanyDataForPdf;
import com.dataart.blueprintsmanager.pdf.DocumentDataForPdf;
import com.dataart.blueprintsmanager.pdf.PdfDocumentGenerator;
import com.dataart.blueprintsmanager.persistence.entity.DocumentEntity;
import com.dataart.blueprintsmanager.persistence.entity.DocumentType;
import com.dataart.blueprintsmanager.persistence.entity.UserEntity;
import com.dataart.blueprintsmanager.persistence.repository.DocumentRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
                .code("")
                .numberInProject(1)
                .typeId(DocumentType.COVER_PAGE.getId())
                .name("Обложка")
                .reassemblyRequired(true)
                .editTime(LocalDateTime.now())
                .build();
        byte[] coverPageTemplate = documentTypeService.getDocumentTemplateByTypeId(document.getTypeId()).get(0);
        reassembleCoverPage(save(document, coverPageTemplate).getId());
    }

    public DocumentDto save(DocumentDto documentForSave, byte[] documentTemplate) {
        DocumentEntity documentEntity = documentForSave.updateEntity(getEmpty());
        documentEntity.setContentInPdf(documentTemplate);
        return new DocumentDto(documentRepository.createTransactional(documentEntity));
    }

    private void reassembleCoverPage(Long documentId) {
        DocumentEntity document = documentRepository.fetchByIdTransactional(documentId);
        if (document.getReassemblyRequired()) {
            byte[] contentInPdf = documentRepository.fetchContentInPdfByDocumentId(document.getId());
            PdfDocumentGenerator coverPageGenerator = new PdfDocumentGenerator(contentInPdf);
            byte[] coverPageInPdf = coverPageGenerator.getFilledA4CoverDocument(getDocumentDataForPdf(document)).getPdfDocumentInBytes();
            documentRepository.updateDocumentInPdfTransactional(documentId, coverPageInPdf);
        }
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
                .designerName(Optional.ofNullable(document.getDesigner()).map(UserEntity::getLastName).orElse(document.getProject().getDesigner().getLastName()))
                .designerSign(Optional.ofNullable(document.getDesigner()).map(UserEntity::getSignature).orElse(document.getProject().getDesigner().getSignature()))
                .supervisorName(Optional.ofNullable(document.getSupervisor()).map(UserEntity::getLastName).orElse(document.getProject().getSupervisor().getLastName()))
                .supervisorSign(Optional.ofNullable(document.getSupervisor()).map(UserEntity::getSignature).orElse(document.getProject().getSupervisor().getSignature()))
                .controllerName(document.getProject().getController().getLastName())
                .controllerSign(document.getProject().getController().getSignature())
                .chiefEngineerName(document.getProject().getChief().getLastName())
                .chiefEngineerSign(document.getProject().getChief().getSignature())
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
                map(d -> {DocumentDto documentDto = new DocumentDto(d);
                documentDto.setDocumentFullCode(getFullCode(d));
                return documentDto;}).
                collect(Collectors.toList());
    }

    public DocumentDto getById(Long documentId) {
        return new DocumentDto(documentRepository.fetchByIdTransactional(documentId));
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
        try {
            Files.write(Paths.get("./target/coverPage.pdf"), documentInPdf);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
}
