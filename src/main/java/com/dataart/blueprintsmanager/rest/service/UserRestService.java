package com.dataart.blueprintsmanager.rest.service;

import com.dataart.blueprintsmanager.aop.track.ParamName;
import com.dataart.blueprintsmanager.aop.track.UserAction;
import com.dataart.blueprintsmanager.aop.track.UserActivityTracker;
import com.dataart.blueprintsmanager.persistence.entity.UserEntity;
import com.dataart.blueprintsmanager.rest.dto.*;
import com.dataart.blueprintsmanager.rest.mapper.RoleMapper;
import com.dataart.blueprintsmanager.rest.mapper.UserMapper;
import com.dataart.blueprintsmanager.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
@AllArgsConstructor
public class UserRestService {
    private final UserService userService;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    public Page<UserDto> getAllNotDeletedPaginated(Pageable pageable) {
        log.info("Try to get {} page with {} users", pageable.getPageNumber(), pageable.getPageSize());
        Page<UserEntity> userEntityPage = userService.getAllNotDeletedPaginated(pageable);
        List<UserDto> userDtoList = userEntityPage.getContent().stream().map(userMapper::userEntityToUserDto).toList();
        Page<UserDto> userDtoPage = new PageImpl<>(userDtoList, pageable, userEntityPage.getTotalElements());
        log.info("Page {} with {} user found", userDtoPage.getNumber(), userDtoPage.getNumberOfElements());
        return userDtoPage;
    }

    @UserActivityTracker(action = UserAction.REG_USER, login = "#user.getLogin()")
    public UserDto registerByAdmin(@ParamName("user") UserRegistrationDto user, MultipartFile file) {
        log.info("Try to create new User with Login = {} by Admin", user.getLogin());
        UserEntity createdUser = userService.createUserByAdmin(userMapper.userRegDtoToUserEntity(user), file);
        UserDto createdUserDto = userMapper.userEntityToUserDto(createdUser);
        log.info("User with Login {} created with ID = {}", createdUserDto.getLogin(), createdUserDto.getId());
        return createdUserDto;
    }

    @UserActivityTracker(action = UserAction.REG_USER, login = "#user.getLogin()")
    public UserDto userSelfRegistration(@ParamName("user") UserRegistrationDto user, MultipartFile file) {
        log.info("Try to create new User with Login = {} by User", user.getLogin());
        UserEntity createdUser = userService.userSelfRegistration(userMapper.userRegDtoToUserEntity(user), file);
        UserDto createdUserDto = userMapper.userEntityToUserDto(createdUser);
        log.info("User with Login {} created with ID = {}", createdUserDto.getLogin(), createdUserDto.getId());
        return createdUserDto;
    }

    @UserActivityTracker(action = UserAction.LOGIN, login = "#user.getLogin()")
    public AuthResponseDto authentication(@ParamName("user") UserAuthDto user) {
        log.info("Try to authenticate User with Login = {}", user.getLogin());
        UserEntity userEntity = userService.userAuthentication(userMapper.userAuthDtoToUserEntity(user));
        AuthResponseDto authResponse = new AuthResponseDto(userEntity.getAccessToken(), userEntity.getRefreshToken());
        log.info("Access granted for User with Login = {} ", user.getLogin());
        return authResponse;
    }

    @UserActivityTracker(action = UserAction.REFRESH_TOKEN)
    public AuthResponseDto tokenAuthentication(AuthRequestByTokenDto request) {
        log.info("Try to authenticate User by REFRESH TOKEN = {}", request.getToken());
        UserEntity user = userService.userTokenAuthentication(request.getToken());
        AuthResponseDto authResponse = new AuthResponseDto(user.getAccessToken(), user.getRefreshToken());
        log.info("Tokens was refreshed for User with Login = {} ", user.getLogin());
        return authResponse;
    }

    @UserActivityTracker(action = UserAction.LOGOUT)
    public void logout() {
        UserEntity currentUser = userService.getCurrentUser();
        log.info("Try to logout current User with Login = {}", currentUser.getLogin());
        userService.logoutById(currentUser.getId());
        log.info("User with Login = {} was logged out ", currentUser.getLogin());
    }

    @UserActivityTracker(action = UserAction.UPDATE_USER, userId = "#userId.toString")
    public UserDto update(@ParamName("userId") Long userId, UserDto userDto, MultipartFile file) {
        log.info("Try to update user with ID = {}", userId);
        userDto.setId(userId);
        UserEntity updatedUser = userService.update(userMapper.userDtoToUserEntity(userDto), file);
        UserDto updatedUserDto = userMapper.userEntityToUserDto(updatedUser);
        log.info("User with ID = {} updated", userId);
        return updatedUserDto;
    }

    @UserActivityTracker(action = UserAction.UPDATE_ROLES, userId = "#userId.toString")
    public UserDto updateRoles(@ParamName("userId") Long userId, Set<RoleDto> roles) {
        log.info("Try to update roles for user with ID = {}", userId);
        UserEntity updatedUser = userService.updateRolesByUserId(roleMapper.roleDtoSetToRoleEntity(roles), userId);
        UserDto updatedUserDto = userMapper.userEntityToUserDto(updatedUser);
        log.info("Roles in User with ID = {} updated", userId);
        return updatedUserDto;
    }
}
