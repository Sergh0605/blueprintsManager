package com.dataart.blueprintsmanager.controller;

import com.dataart.blueprintsmanager.dto.CompanyDto;
import com.dataart.blueprintsmanager.dto.ProjectDto;
import com.dataart.blueprintsmanager.dto.StageDto;
import com.dataart.blueprintsmanager.dto.UserDto;
import com.dataart.blueprintsmanager.service.CompanyService;
import com.dataart.blueprintsmanager.service.ProjectService;
import com.dataart.blueprintsmanager.service.StageService;
import com.dataart.blueprintsmanager.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
@Slf4j
public class ProjectController {
    private final ProjectService projectService;
    private final CompanyService companyService;
    private final UserService userService;
    private final StageService stageService;

    public ProjectController(ProjectService projectService, CompanyService companyService, UserService userService, StageService stageService) {
        this.projectService = projectService;
        this.companyService = companyService;
        this.userService = userService;
        this.stageService = stageService;
    }

    @GetMapping(value = {"/", "/index", "/project"})
    public String index(Model model) {
        List<ProjectDto> projects = projectService.fetchAll();
        model.addAttribute("projects", projects);
        return "index";
    }

    @GetMapping(value = {"/project/view/{projectId}"})
    public String viewProject(@PathVariable Long projectId, Model model) {
        ProjectDto project = projectService.getById(projectId);
        return getProjectPage(model, project.getCompanyId(), true, true, project);
    }

    @GetMapping(value = {"/project/edit/{projectId}"})
    public String editProject(@PathVariable Long projectId, Model model) {
        ProjectDto project = projectService.getById(projectId);
        return getProjectPage(model, project.getCompanyId(), false, true, project);
    }

    @GetMapping(value = {"/project/new"})
    public String newProject(@RequestParam(required = false) Long companyId, Model model) {
        ProjectDto project = projectService.getNew();
        if (companyId != null) {
            CompanyDto company = companyService.fetchById(companyId);
            project.setCompanyId(company.getId());
        }
        return getProjectPage(model, companyId, false, false, project);
    }

    @PostMapping("/project/save")
    public String saveProject(@ModelAttribute("project") ProjectDto project, Model model) {
        return "redirect:/project/view/" + project.getId();
    }

    private String getProjectPage(Model model, Long companyId, boolean fieldsIsDisabled, boolean projectExists, ProjectDto project) {
        List<CompanyDto> companies = companyService.fetchAll();
        List<UserDto> users = userService.fetchAllByCompanyId(companyId);
        List<StageDto> stages = stageService.fetchAll();
        model.addAttribute("disabled", fieldsIsDisabled);
        model.addAttribute("projectExists", projectExists);
        model.addAttribute("project", project);
        model.addAttribute("users", users);
        model.addAttribute("companies", companies);
        model.addAttribute("stages", stages);
        return "project";
    }
}
