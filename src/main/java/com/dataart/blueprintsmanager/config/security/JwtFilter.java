package com.dataart.blueprintsmanager.config.security;

import com.dataart.blueprintsmanager.exceptions.AuthenticationApplicationException;
import com.dataart.blueprintsmanager.rest.dto.ExceptionDto;
import com.dataart.blueprintsmanager.service.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

import static org.springframework.util.StringUtils.hasText;

@Component
@AllArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    public static final String AUTHORIZATION = "Authorization";
    private final TokenService tokenService;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = getTokenFromRequest(request);
            if (token != null) {
                Claims claims = tokenService.getValidAccessTokenClaims(token);
                String userLogin = claims.getSubject();
                CustomUserDetails customUserDetails = customUserDetailsService.loadUserByUsername(userLogin);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
            filterChain.doFilter(request, response);
        } catch (AuthenticationApplicationException e) {
            log.warn(e.getMessage(), e);
            getExceptionResponse(response, request.getRequestURI(), e, HttpStatus.UNAUTHORIZED);
        }
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearer = request.getHeader(AUTHORIZATION);
        if (hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    private void getExceptionResponse(HttpServletResponse response, String requestUri, Exception e, HttpStatus status) throws IOException {
        ExceptionDto exceptionDto = ExceptionDto.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(e.getMessage())
                .path(requestUri)
                .build();
        ObjectMapper objMap = new ObjectMapper();
        objMap.registerModule(new JavaTimeModule());
        objMap.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String exceptionJsonString = objMap.writeValueAsString(exceptionDto);
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status.value());
        out.print(exceptionJsonString);
        out.flush();
    }
}
