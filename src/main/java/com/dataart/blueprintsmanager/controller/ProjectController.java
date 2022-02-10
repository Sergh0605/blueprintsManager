package com.dataart.blueprintsmanager.controller;

import com.dataart.blueprintsmanager.dto.*;
import com.dataart.blueprintsmanager.service.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
@Slf4j
@AllArgsConstructor
public class ProjectController {
    private final ProjectService projectService;
    private final CompanyService companyService;
    private final UserService userService;
    private final StageService stageService;
    private final DocumentService documentService;

    @GetMapping(value = {"/", "/index", "/project"})
    public String index(Model model) {
        List<ProjectDto> projects = projectService.getAll();
        model.addAttribute("projects", projects);
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
        ProjectDto project = projectService.getNew();
        if (companyId != null) {
            CompanyDto company = companyService.getById(companyId);
            project.setCompanyId(company.getId());
        }
        return getProjectPage(project, model, companyId, false, false);
    }

    @PostMapping("/project/save")
    public String saveProject(@ModelAttribute("project") ProjectDto project) {
        Long projectId;
        if (project.getId() == null) {
            projectId = projectService.save(project).getId();
        } else projectId = projectService.update(project).getId();
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
