package com.dataart.blueprintsmanager.controller;

import com.dataart.blueprintsmanager.dto.DocumentDto;
import com.dataart.blueprintsmanager.service.DocumentService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@Slf4j
@AllArgsConstructor
public class DocumentController {
    DocumentService documentService;

    @GetMapping(value = {"/document/download/{documentId}"})
    public void serveFile(@PathVariable Long documentId, HttpServletResponse response) {
        DocumentDto document = documentService.getFileForDownload(documentId);
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename = " + document.getDocumentFileName();
        response.setHeader(headerKey, headerValue);
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            outputStream.write(document.getDocumentInPdf());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
