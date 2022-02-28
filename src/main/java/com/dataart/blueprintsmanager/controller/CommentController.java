package com.dataart.blueprintsmanager.controller;

import com.dataart.blueprintsmanager.dto.CommentDto;
import com.dataart.blueprintsmanager.service.CommentService;
import com.dataart.blueprintsmanager.util.CustomPage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "5") Integer size,
            @PathVariable Long projectId) {
        int currentPage = page;
        int pageSize = size;
        CustomPage<CommentDto> commentPage = commentService.getPageOfCommentsForProject(projectId, PageRequest.of(currentPage, pageSize));
        CommentDto newComment = CommentDto.builder().login("Petrov").projectId(projectId).build();
        model.addAttribute("commentPage", commentPage);
        model.addAttribute("newComment", newComment);
        int totalPages = commentPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
            model.addAttribute("projectId", projectId);
        }
        return "projectComments";
    }

    @GetMapping(value = "/comment/document/{documentId}")
    public String documentCommentsPage(
            Model model,
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "5") Integer size,
            @PathVariable Long documentId) {
        int currentPage = page;
        int pageSize = size;
        CustomPage<CommentDto> commentPage = commentService.getPageOfCommentsForDocument(documentId, PageRequest.of(currentPage, pageSize));
        CommentDto newComment = CommentDto.builder().login("Petrov").documentId(documentId).build();
        model.addAttribute("commentPage", commentPage);
        model.addAttribute("newComment", newComment);
        int totalPages = commentPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
            model.addAttribute("documentId", documentId);
        }
        return "documentComments";
    }

    @PostMapping("/comment/project/save")
    public String saveForProject(@ModelAttribute("comment") CommentDto comment) {
            CommentDto createdComment = commentService.create(comment);
        return "redirect:/comment/project/" + createdComment.getProjectId();
    }

    @PostMapping("/comment/document/save")
    public String saveForDocument(@ModelAttribute("comment") CommentDto comment) {
        CommentDto createdComment = commentService.create(comment);
        return "redirect:/comment/document/" + createdComment.getDocumentId();
    }
}
