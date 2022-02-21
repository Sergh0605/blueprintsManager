package com.dataart.blueprintsmanager.controller;

import com.dataart.blueprintsmanager.dto.DocumentDto;
import com.dataart.blueprintsmanager.dto.ProjectDto;
import com.dataart.blueprintsmanager.dto.UserDto;
import com.dataart.blueprintsmanager.exceptions.EditDocumentException;
import com.dataart.blueprintsmanager.persistence.entity.DocumentType;
import com.dataart.blueprintsmanager.service.DocumentService;
import com.dataart.blueprintsmanager.service.DocumentTypeService;
import com.dataart.blueprintsmanager.service.ProjectService;
import com.dataart.blueprintsmanager.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.LocalDateTime;
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
                               @ModelAttribute("document") @Valid DocumentDto document,
                               BindingResult result,
                               Model model) {
        document.setEditTime(LocalDateTime.now());
        if (document.getReassemblyRequired() == null) {
            document.setReassemblyRequired(false);
        }
        if (result.hasErrors()) {
            model.addAttribute("document", document);
            return getDocumentPage(document, model, false, document.getId() != null);
        }
        Long documentId;
        if (document.getId() == null) {
            try {
                documentId = documentService.createEditableDocumentForSave(document, file).getId();
                return "redirect:/document/view/" + documentId;
            } catch (EditDocumentException e) {
                result.addError(new FieldError("document","documentFileName", e.getMessage()));
                return getDocumentPage(document, model, false, document.getId() != null);
            }
        } else {
            try {
                documentId = documentService.update(document, file).getId();
                return "redirect:/document/view/" + documentId;
            } catch (EditDocumentException e) {
                result.addError(new FieldError("document","documentFileName", e.getMessage()));
                return getDocumentPage(document, model, false, document.getId() != null);
            }
        }
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
