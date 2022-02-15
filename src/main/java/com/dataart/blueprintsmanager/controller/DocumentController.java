package com.dataart.blueprintsmanager.controller;

import com.dataart.blueprintsmanager.dto.DocumentDto;
import com.dataart.blueprintsmanager.dto.ProjectDto;
import com.dataart.blueprintsmanager.dto.UserDto;
import com.dataart.blueprintsmanager.exceptions.CustomApplicationException;
import com.dataart.blueprintsmanager.persistence.entity.DocumentType;
import com.dataart.blueprintsmanager.service.DocumentService;
import com.dataart.blueprintsmanager.service.DocumentTypeService;
import com.dataart.blueprintsmanager.service.ProjectService;
import com.dataart.blueprintsmanager.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Controller
@Slf4j
@AllArgsConstructor
public class DocumentController {
    DocumentService documentService;
    UserService userService;
    ProjectService projectService;
    DocumentTypeService documentTypeService;

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
            log.debug(e.getMessage());
            throw new CustomApplicationException("Broken file for download");
        }
    }

    @GetMapping(value = {"/document/assemble/{documentId}"})
    public String reassemble(@PathVariable Long documentId, Model model) {
        documentService.reassembleDocument(documentId);
        return "redirect:/document/view/" + documentId;
    }

    @GetMapping(value = {"/document/view/{documentId}"})
    public String view(@PathVariable Long documentId, Model model) {
        DocumentDto document = documentService.getById(documentId);
        return getDocumentPage(document, model, true, true);
    }

    private String getDocumentPage(DocumentDto document, Model model, boolean fieldsIsDisabled, boolean documentExists) {
        ProjectDto project = projectService.getDtoById(document.getProjectId());
        List<UserDto> users = userService.getAllByCompanyId(project.getCompanyId());
        List<DocumentType> documentTypes = documentTypeService.getAll();
        if (document.getDesignerId() == null) document.setDesignerId(project.getDesignerId());
        if (document.getSupervisorId() == null) document.setSupervisorId(project.getSupervisorId());
        boolean unmodified = documentTypeService.getAllUnmodified().contains(document.getType());
        model.addAttribute("document", document);
        model.addAttribute("disabled", fieldsIsDisabled);
        model.addAttribute("documentExists", documentExists);
        model.addAttribute("users", users);
        model.addAttribute("types", documentTypes);
        model.addAttribute("unmodified", unmodified);
        return "document";
    }

}
