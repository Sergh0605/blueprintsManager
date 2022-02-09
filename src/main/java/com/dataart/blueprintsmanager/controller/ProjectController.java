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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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

    @GetMapping(value = {"/project/{projectId}"})
    public String editProject(@PathVariable Long projectId, Model model) {
        if (projectId == 0L) {
            return newProject(projectId, 0L, model);
        }
        ProjectDto project = projectService.getById(projectId);
        List<CompanyDto> companies = companyService.fetchAll();
        List<UserDto> users = userService.fetchAllByCompanyId(project.getCompanyId());
        List<StageDto> stages = stageService.fetchAll();
        model.addAttribute("project", project);
        model.addAttribute("users", users);
        model.addAttribute("companies", companies);
        model.addAttribute("stages", stages);
        return "project";
    }

    @GetMapping(value = {"/project/{projectId}/{companyId}"})
    public String newProject(@PathVariable Long projectId, @PathVariable Long companyId, Model model) {
        ProjectDto project = projectService.getById(projectId);
        if (project.getId() == 0) {
            if (companyId > 0) {
                CompanyDto company = companyService.fetchById(companyId);
                project.setCompanyId(company.getId());
            } else project.setCompanyId(0L);
        }
        List<CompanyDto> companies = companyService.fetchAll();
        List<UserDto> users = userService.fetchAllByCompanyId(companyId);
        project.setCompanyId(companyId);
        List<StageDto> stages = stageService.fetchAll();
        model.addAttribute("project", project);
        model.addAttribute("users", users);
        model.addAttribute("companies", companies);
        model.addAttribute("stages", stages);
        return "project";
    }
}
