package com.dataart.blueprintsmanager.rest.controller;

import com.dataart.blueprintsmanager.rest.service.ActivityRestService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/activity")
@Slf4j
@AllArgsConstructor
public class ActivityController {
    private final ActivityRestService activityRestService;

    @GetMapping
    public ResponseEntity<?> getAllPageable(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "5") Integer size,
            @RequestParam(name = "search", defaultValue = "") String search) {
        return ResponseEntity.ok(activityRestService.getAllFilteredPaginated(search, PageRequest.of(page, size)));
    }
}
