package com.dataart.blueprintsmanager.controller;

import com.dataart.blueprintsmanager.dto.CompanyDto;
import com.dataart.blueprintsmanager.dto.ProjectDto;
import com.dataart.blueprintsmanager.dto.UserDto;
import com.dataart.blueprintsmanager.service.CompanyService;
import com.dataart.blueprintsmanager.service.ProjectService;
import com.dataart.blueprintsmanager.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;


@Controller
@Slf4j
public class ProjectController {
    private final ProjectService projectService;
    private final CompanyService companyService;
    private final UserService userService;

    public ProjectController(ProjectService projectService, CompanyService companyService, UserService userService) {
        this.projectService = projectService;
        this.companyService = companyService;
        this.userService = userService;
    }

    @GetMapping(value = {"/", "/index", "/project"})
    public String index(Model model) {
        List<ProjectDto> projects = projectService.fetchAll();
        model.addAttribute("projects", projects);
        return "index";
    }

    @GetMapping(value = {"/project/new/{id}"})
    public String newProject(@PathVariable Long id, Model model) {
        ProjectDto project = projectService.getNew();
        project.setCompanyId(id);
        List<CompanyDto> companies = companyService.fetchAll();
        List<UserDto> users = userService.fetchAllByCompanyId(id);
        model.addAttribute("project", project);
        model.addAttribute("users", users);
        model.addAttribute("selectedCompany", id);
        model.addAttribute("companies", companies);
        return "project";
    }

    @GetMapping(value = {"/project/new2"})
    public String companySelector(Model model) {
        ProjectDto project = projectService.getNew();
        List<UserDto> users = new ArrayList<>();
        List<CompanyDto> companies = companyService.fetchAll();
        model.addAttribute("companies", companies);
        model.addAttribute("users", users);
        model.addAttribute("project", project);
        return "project";
    }
}
