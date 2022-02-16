package com.dataart.blueprintsmanager.service;

import com.dataart.blueprintsmanager.persistence.entity.DocumentType;
import com.dataart.blueprintsmanager.persistence.repository.DocumentTypeRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    public void updatePdfTemplates() throws IOException {
        String pathToCoverPage = "./src/main/resources/pdfTemplates/A4CoverTemplate.pdf";
        String pathToTitleList = "./src/main/resources/pdfTemplates/A4TitleListTemplate.pdf";
        String pathToTextTemplate = "./src/main/resources/pdfTemplates/A4TextTemplate.pdf";
        String pathToSecondPageTemplatePdf = "./src/main/resources/pdfTemplates/A4GeneralPageTemplate.pdf";
        String pathToDrawingTemplatePdf = "./src/main/resources/pdfTemplates/A4Drawing2P.pdf";
        byte[] coverPage = Files.readAllBytes(Paths.get(pathToCoverPage));
        byte[] titlePage = Files.readAllBytes(Paths.get(pathToTitleList));
        byte[] textF = Files.readAllBytes(Paths.get(pathToTextTemplate));
        byte[] textS = Files.readAllBytes(Paths.get(pathToSecondPageTemplatePdf));
        byte[] drawing = Files.readAllBytes(Paths.get(pathToDrawingTemplatePdf));
        documentTypeRepository.updateTemplate(1L, coverPage, null);
        documentTypeRepository.updateTemplate(2L, titlePage, null);
        documentTypeRepository.updateTemplate(3L, textF, textS);
        documentTypeRepository.updateTemplate(4L, textF, textS);
        documentTypeRepository.updateTemplate(5L, drawing, null);
    }
}
