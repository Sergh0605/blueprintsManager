package com.dataart.blueprintsmanager.controller;

import com.dataart.blueprintsmanager.dto.*;
import com.dataart.blueprintsmanager.exceptions.EditProjectException;
import com.dataart.blueprintsmanager.service.*;
import com.dataart.blueprintsmanager.util.CustomPage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Controller
@Slf4j
@AllArgsConstructor
public class ProjectController {
    private final ProjectService projectService;
    private final CompanyService companyService;
    private final UserService userService;
    private final StageService stageService;
    private final DocumentService documentService;


    public String index(Model model) throws IOException {
        List<ProjectDto> projects = projectService.getAll();
        model.addAttribute("projects", projects);
        return "index";
    }

    @GetMapping(value = {"/", "/index", "/project"})
    public String indexPageable(
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size) {
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(5);
        CustomPage<ProjectDto> projectPage = projectService.getAllPaginated(PageRequest.of(currentPage, pageSize));
        model.addAttribute("projectPage", projectPage);
        int totalPages = projectPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }
        return "index";
    }

    @GetMapping(value = {"/project/view/{projectId}"})
    public String viewProject(@PathVariable Long projectId, Model model) {
        ProjectDto project = projectService.getDtoById(projectId);
        return getProjectPage(project, model, project.getCompanyId(), true, true);
    }

    @GetMapping(value = {"/project/edit/{projectId}"})
    public String editProject(@PathVariable Long projectId, Model model) {
        ProjectDto project = projectService.getDtoById(projectId);
        return getProjectPage(project, model, project.getCompanyId(), false, true);
    }

    @GetMapping(value = {"/project/new"})
    public String newProject(@RequestParam(required = false) Long companyId, Model model) {
        ProjectDto project = projectService.prepareNewProjectDto();
        if (companyId != null) {
            CompanyDto company = companyService.getById(companyId);
            project.setCompanyId(company.getId());
        }
        return getProjectPage(project, model, companyId, false, false);
    }

    @GetMapping(value = {"/project/delete/{projectId}"})
    public String deleteProject(@PathVariable Long projectId, Model model) {
        projectService.deleteById(projectId);
        return "redirect:/project";
    }

    @PostMapping("/project/save")
    public String saveProject(@ModelAttribute("project") @Valid ProjectDto project,
                              BindingResult result,
                              Model model) {
        project.setEditTime(LocalDateTime.now());
        if (project.getReassemblyRequired() == null) {
            project.setReassemblyRequired(false);
        }
        if (result.hasErrors()) {
            return getProjectPage(project, model, project.getCompanyId(), false, project.getId() != null);
        }
        Long projectId;
        if (project.getId() == null) {
            try {
                projectId = projectService.create(project).getId();
                return "redirect:/project/view/" + projectId;
            } catch (EditProjectException e) {
                result.addError(new FieldError("project","code", "Project with such code already exists"));
                return getProjectPage(project, model, project.getCompanyId(), false, project.getId() != null);
            }
        } else {
            try {
                projectId = projectService.update(project).getId();
                return "redirect:/project/view/" + projectId;
            } catch (EditProjectException e) {
                result.addError(new FieldError("project","code", "Project with such code already exists"));
                return getProjectPage(project, model, project.getCompanyId(), false, project.getId() != null);
            }
        }
    }

    @GetMapping(value = {"/project/download/{projectId}"})
    public void serveFile(@PathVariable Long projectId, HttpServletResponse response) {
        projectService.getFileForDownload(projectId, response);
    }

    @GetMapping(value = {"/project/assemble/{projectId}"})
    public String reassemble(@PathVariable Long projectId, Model model) {
        projectService.reassemble(projectId);
        return "redirect:/project/view/" + projectId;
    }

    private String getProjectPage(ProjectDto project, Model model, Long companyId, boolean fieldsIsDisabled, boolean projectExists) {
        List<CompanyDto> companies = companyService.getAll();
        List<UserDto> users = userService.getAllByCompanyId(companyId);
        List<StageDto> stages = stageService.getAll();
        List<DocumentDto> documents = documentService.getAllByProjectId(project.getId());
        model.addAttribute("documents", documents);
        model.addAttribute("disabled", fieldsIsDisabled);
        model.addAttribute("projectExists", projectExists);
        model.addAttribute("project", project);
        model.addAttribute("users", users);
        model.addAttribute("companies", companies);
        model.addAttribute("stages", stages);
        return "project";
    }
}
