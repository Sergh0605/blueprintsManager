package com.dataart.blueprintsmanager.controller;

import com.dataart.blueprintsmanager.dto.CommentDto;
import com.dataart.blueprintsmanager.service.CommentService;
import com.dataart.blueprintsmanager.util.CustomPage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@Slf4j
@AllArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @GetMapping(value = "/comment/project/{projectId}")
    public String projectCommentsPage(
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @PathVariable Long projectId) {
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(5);
        CustomPage<CommentDto> commentPage = commentService.getPageOfCommentsForProject(projectId, PageRequest.of(currentPage, pageSize));
        model.addAttribute("commentPage", commentPage);

        int totalPages = commentPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
            model.addAttribute("projectId", projectId);
        }

        return "comments";
    }
}
