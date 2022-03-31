package com.dataart.blueprintsmanager.rest.controller;

import com.dataart.blueprintsmanager.rest.dto.AuthRequestByTokenDto;
import com.dataart.blueprintsmanager.rest.dto.UserAuthDto;
import com.dataart.blueprintsmanager.rest.service.UserRestService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/user")
@Slf4j
@AllArgsConstructor
public class AuthenticationController {
    private final UserRestService userRestService;

    @PostMapping("/auth")
    public ResponseEntity<?> authenticate(@RequestBody @Valid UserAuthDto user) {
        return ResponseEntity.ok(userRestService.getAuthentication(user));
    }

    @PostMapping("/refresh_token_auth")
    public ResponseEntity<?> refreshTokens(@RequestBody @Valid AuthRequestByTokenDto request) {

        return ResponseEntity.ok(userRestService.refreshUserTokens(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        userRestService.logout();
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
