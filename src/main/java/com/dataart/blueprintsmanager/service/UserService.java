package com.dataart.blueprintsmanager.service;

import com.dataart.blueprintsmanager.exceptions.AuthenticationApplicationException;
import com.dataart.blueprintsmanager.exceptions.InvalidInputDataException;
import com.dataart.blueprintsmanager.exceptions.NotFoundCustomApplicationException;
import com.dataart.blueprintsmanager.persistence.entity.FileEntity;
import com.dataart.blueprintsmanager.persistence.entity.Role;
import com.dataart.blueprintsmanager.persistence.entity.RoleEntity;
import com.dataart.blueprintsmanager.persistence.entity.UserEntity;
import com.dataart.blueprintsmanager.persistence.repository.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final CompanyService companyService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Transactional(readOnly = true)
    public Page<UserEntity> getAllNotDeletedPaginated(Pageable pageable) {
        return userRepository.findAllByDeletedOrderByCompany(false, pageable);
    }

    @Transactional(readOnly = true)
    public UserEntity getById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> {
            throw new NotFoundCustomApplicationException(String.format("User with ID = %d not found", userId));
        });
    }

    @Transactional(readOnly = true)
    public UserEntity getByIdAndCompanyId(Long userId, Long companyId) {
        return userRepository.findByIdAndCompanyId(userId, companyId).orElseThrow(() -> {
            throw new NotFoundCustomApplicationException(String.format("User with ID = %d not found for Company with ID = %d", userId, companyId));
        });
    }

    @Transactional(readOnly = true)
    public List<UserEntity> getAllByCompanyId(Long companyId) {
        return userRepository.findAllByCompanyIdOrderByLastName(companyId);
    }

    @Transactional(readOnly = true)
    public UserEntity getByLogin(String login) {
        return userRepository.findByLogin(login).orElseThrow(() -> {
            throw new NotFoundCustomApplicationException(String.format("User with Login = %s not found", login));
        });
    }

    private UserEntity getByLoginAndPassword(UserEntity inputUserEntity) {
        UserEntity validUser = getByLogin(inputUserEntity.getLogin());
        if (passwordEncoder.matches(inputUserEntity.getPassword(), validUser.getPassword())) {
            return validUser;
        }
        throw new NotFoundCustomApplicationException(String.format("Incorrect password for user with Login = %s", inputUserEntity.getLogin()));
    }

    @Transactional
    public UserEntity setDeletedById(Long id, Boolean deletedStatus) {
        UserEntity userForChangeDeleteStatus = getById(id);
        userForChangeDeleteStatus.setDeleted(deletedStatus);
        if (deletedStatus) {
            tokenService.disableByUserId(userForChangeDeleteStatus.getId());
        }
        return userRepository.save(userForChangeDeleteStatus);
    }

    @Transactional
    public UserEntity createUserByAdmin(UserEntity user, MultipartFile signFile) {
        fillMainFields(user, signFile);
        if (user.getRoles() != null) {
            user.setRoles(user.getRoles().stream().map(r -> roleService.getById(r.getId())).collect(Collectors.toSet()));
        } else {
            user.setRoles(Set.of(roleService.getById(3L)));
        }
        return userRepository.save(user);
    }

    @Transactional
    public UserEntity userSelfRegistration(UserEntity user, MultipartFile signFile) {
        fillMainFields(user, signFile);
        user.setRoles(Set.of(roleService.getById(3L)));
        return userRepository.save(user);
    }

    private void fillMainFields(UserEntity user, MultipartFile signFile) {
        if (existsByLogin(user.getLogin())) {
            throw new InvalidInputDataException(String.format("Can't create User. User with Login = %s is already exists", user.getLogin()));
        }
        user.setCompany(companyService.getById(user.getCompany().getId()));
        user.setDeleted(false);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setSignatureFile(new FileEntity());
        setSignToUser(user, signFile);
    }

    private void setSignToUser(UserEntity user, MultipartFile signFile) {
        if (signFile != null) {
            if (!signFile.isEmpty() && signFile.getContentType().contains("image")) {
                try {
                    user.getSignatureFile().setFileInBytes(signFile.getInputStream().readAllBytes());
                } catch (IOException e) {
                    log.debug(e.getMessage(), e);
                    throw new InvalidInputDataException(String.format("Can't save User with Login = %s. Broken sign file", user.getLogin()), e);
                }
                throw new InvalidInputDataException("Can't save User. Wrong type of sign file.");
            }
        }
    }

    private boolean existsByLogin(String login) {
        return userRepository.existsByLogin(login);
    }

    @Transactional
    public UserEntity userAuthentication(UserEntity userToAuth) {
        try {
            UserEntity userEntity = getByLoginAndPassword(userToAuth);
            tokenService.disableByUserId(userEntity.getId());
            userEntity.setAccessToken(tokenService.generateAccessToken(userEntity));
            userEntity.setRefreshToken(tokenService.generateRefreshToken(userEntity));
            return userEntity;
        } catch (NotFoundCustomApplicationException e) {
            log.warn(e.getMessage(), e);
            throw new AuthenticationApplicationException("Incorrect login or password.", e);
        }
    }

    @Transactional
    public UserEntity userTokenAuthentication(String token) {
        Claims claims = tokenService.validateRefreshToken(token);
        if (claims != null) {
            UserEntity userEntity = getByLogin(claims.getSubject());
            if (!userEntity.getDeleted()) {
                tokenService.disableByUserId(userEntity.getId());
                userEntity.setAccessToken(tokenService.generateAccessToken(userEntity));
                userEntity.setRefreshToken(tokenService.generateRefreshToken(userEntity));
                return userEntity;
            }
        }
        throw new AuthenticationApplicationException(String.format("Invalid token = %s", token));
    }

    @Transactional(readOnly = true)
    public UserEntity getCurrentUser() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails springSecurityUser) {
            return getByLogin(springSecurityUser.getUsername());
        }
        throw new NotFoundCustomApplicationException("Current user not found.");
    }

    @Transactional
    public void logoutById(Long userId) {
        tokenService.disableByUserId(userId);
    }

    @Transactional
    public UserEntity update(UserEntity userForUpdate, MultipartFile signFile) {
        UserEntity currentUser = getCurrentUser();
        RoleEntity adminRole = roleService.getByName(Role.ADMIN);
        if (currentUser.getId().equals(userForUpdate.getId()) || currentUser.getRoles().contains(adminRole)) {
            UserEntity currentUserForUpdate = getById(userForUpdate.getId());
            if (!currentUserForUpdate.getLogin().equals(userForUpdate.getLogin())) {
                if (existsByLogin(userForUpdate.getLogin())) {
                    throw new InvalidInputDataException(String.format("Can't save User. User with Login = %s is already exists", userForUpdate.getLogin()));
                }
            }
            currentUserForUpdate.setLastName(userForUpdate.getLastName());
            currentUserForUpdate.setEmail(userForUpdate.getEmail());
            setSignToUser(currentUserForUpdate, signFile);
            return userRepository.save(currentUserForUpdate);
        }
        throw new AuthenticationApplicationException(String.format("Current user %s have not rights to update user with Login = %s. ", currentUser.getLogin(), userForUpdate.getLogin()));
    }

    @Transactional
    public UserEntity updateRolesByUserId(Set<RoleEntity> rolesForUpdate, Long userId) {
        UserEntity userForUpdate = getById(userId);
        userForUpdate.setRoles(rolesForUpdate.stream().map(r -> roleService.getById(r.getId())).collect(Collectors.toSet()));
        return userRepository.save(userForUpdate);
    }
}
