package com.dataart.blueprintsmanager.controller;

import com.dataart.blueprintsmanager.dto.UserDto;
import com.dataart.blueprintsmanager.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@Slf4j
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = {"/user"})
    public String index(Model model) {
        List<UserDto> users = userService.fetchAll();
        model.addAttribute("users", users);
        return "users";
    }
}
