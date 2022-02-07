package com.dataart.blueprintsmanager.controller;

import com.dataart.blueprintsmanager.dto.ProjectDto;
import com.dataart.blueprintsmanager.service.ProjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;


@Controller
@Slf4j
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping(value = {"/", "/index"})
    public String index(Model model) {
        List<ProjectDto> projects = projectService.fetchAll();
        model.addAttribute("projects", projects);
        return "index";
    }
}
