package com.dataart.blueprintsmanager.controller;

import com.dataart.blueprintsmanager.exceptions.CustomApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
@Slf4j
public class GlobalControllerExceptionHandler {

    @ExceptionHandler(value = CustomApplicationException.class)
    public String applicationError(Model model, Exception e) {
        model.addAttribute("warningMessage", e.getMessage());
        return "exceptionPage";
    }

    @ExceptionHandler(Exception.class)
    public String generalError(Model model, Exception e, HttpServletRequest request) {
        log.error("Request: " + request.getRequestURL() + " raised " + e.getMessage(), e);
        model.addAttribute("warningMessage", "Internal Server Error");
        return "exceptionPage";
    }
}
