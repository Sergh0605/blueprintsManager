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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@Slf4j
@AllArgsConstructor
public class DocumentController {
    private final DocumentService documentService;
    private final UserService userService;
    private final ProjectService projectService;
    private final DocumentTypeService documentTypeService;

    @GetMapping(value = {"/document/download/{documentId}"})
    public void serveFile(@PathVariable Long documentId, HttpServletResponse response) {
        documentService.getFileForDownload(documentId, response);
    }

    @GetMapping(value = {"/document/delete/{documentId}"})
    public String deleteDocument(@PathVariable Long documentId, Model model) {
        return "redirect:/project/view/" + documentService.deleteById(documentId);
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

    @GetMapping(value = {"/document/edit/{documentId}"})
    public String edit(@PathVariable Long documentId, Model model) {
        DocumentDto document = documentService.getById(documentId);
        return getDocumentPage(document, model, false, true);
    }

    @GetMapping(value = {"/document/new/{projectId}"})
    public String newDocument(@PathVariable Long projectId, Model model) {
        DocumentDto document = documentService.getNew(projectId);
        return getDocumentPage(document, model, false, false);
    }

    @PostMapping("/document/save")
    public String saveDocument(@RequestParam(value = "file", required = false) MultipartFile file,
                              @ModelAttribute("document") DocumentDto document,
                              RedirectAttributes attributes) {
        Long documentId;
        if (document.getId() == null) {
            try {
                documentId = documentService.createEditableDocumentForSave(document, file).getId();
                return "redirect:/document/view/" + documentId;
            } catch (CustomApplicationException e) {
                attributes.addFlashAttribute("warningMessage", e.getMessage());
                return "redirect:/document/new/" + document.getProjectId();
            }
        } else documentId = documentService.update(document, file).getId();
        return "redirect:/document/view/" + documentId;
    }

    private String getDocumentPage(DocumentDto document, Model model, boolean fieldsIsDisabled, boolean documentExists) {
        ProjectDto project = projectService.getDtoById(document.getProjectId());
        List<UserDto> users = userService.getAllByCompanyId(project.getCompanyId());
        List<DocumentType> documentTypes = documentTypeService.getAll();
        model.addAttribute("document", projectService.setDesignerAndSupervisorInDocument(document));
        model.addAttribute("disabled", fieldsIsDisabled);
        model.addAttribute("documentExists", documentExists);
        model.addAttribute("users", users);
        model.addAttribute("types", documentTypes);
        return "document";
    }

}
