package com.dataart.blueprintsmanager.rest.controller;

import com.dataart.blueprintsmanager.rest.service.RoleRestService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/role")
@Slf4j
@AllArgsConstructor
public class RoleController {
    private final RoleRestService roleRestService;

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(roleRestService.getAllRoles());
    }
}
