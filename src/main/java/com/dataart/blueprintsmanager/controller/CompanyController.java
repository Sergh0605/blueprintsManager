package com.dataart.blueprintsmanager.controller;

import com.dataart.blueprintsmanager.dto.CompanyDto;
import com.dataart.blueprintsmanager.service.CompanyService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@Slf4j
@AllArgsConstructor
public class CompanyController {
    private final CompanyService companyService;

    @GetMapping(value = {"/company"})
    public String index(Model model) {
        List<CompanyDto> companies = companyService.getAll();
        model.addAttribute("companies", companies);
        return "companies";
    }
}
