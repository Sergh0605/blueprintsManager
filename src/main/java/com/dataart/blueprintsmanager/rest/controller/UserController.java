package com.dataart.blueprintsmanager.rest.controller;

import com.dataart.blueprintsmanager.rest.dto.*;
import com.dataart.blueprintsmanager.rest.service.UserRestService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.Set;

@RestController
@RequestMapping("/api/user")
@Slf4j
@AllArgsConstructor
public class UserController {
    private final UserRestService userRestService;

    @GetMapping
    public ResponseEntity<?> getAllPageable(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "5") Integer size) {
        return ResponseEntity.ok(userRestService.getAllNotDeletedPaginated(PageRequest.of(page, size)));
    }

    @PostMapping
    public ResponseEntity<?> registerNewUserByAdmin(@RequestPart @Valid UserRegistrationDto user,
                                                    @RequestPart(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userRestService.registerByAdmin(user, file));
    }

    @PostMapping("/register")
    public ResponseEntity<?> userSelfRegistration(@RequestPart @Valid UserRegistrationDto user,
                                                    @RequestPart(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userRestService.userSelfRegistration(user, file));
    }

    @PutMapping(value = {"/{userId}"})
    public ResponseEntity<?> updateUser(@PathVariable Long userId,
                                          @RequestPart @Valid UserDto user,
                                          @RequestPart(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.ok(userRestService.update(userId, user, file));
    }

    @PutMapping(value = {"/{userId}/edit_roles"})
    public ResponseEntity<?> updateRoles(@PathVariable Long userId,
                                          @RequestBody @Valid Set<RoleDto> roles) {
        return ResponseEntity.ok(userRestService.updateRoles(userId, roles));
    }

    @PostMapping("/auth")
    public ResponseEntity<?> authentication(@RequestBody @Valid UserAuthDto user) {
        return ResponseEntity.ok(userRestService.authentication(user));
    }

    @PostMapping("/refresh_token_auth")
    public ResponseEntity<?> authentication(@RequestBody @Valid AuthRequestByTokenDto request) {
        return ResponseEntity.ok(userRestService.tokenAuthentication(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        userRestService.logout();
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
