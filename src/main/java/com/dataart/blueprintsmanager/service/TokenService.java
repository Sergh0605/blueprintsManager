package com.dataart.blueprintsmanager.service;

import com.dataart.blueprintsmanager.config.security.TokenType;
import com.dataart.blueprintsmanager.exceptions.AuthenticationApplicationException;
import com.dataart.blueprintsmanager.exceptions.NotFoundCustomApplicationException;
import com.dataart.blueprintsmanager.persistence.entity.RoleEntity;
import com.dataart.blueprintsmanager.persistence.entity.TokenEntity;
import com.dataart.blueprintsmanager.persistence.entity.UserEntity;
import com.dataart.blueprintsmanager.persistence.repository.TokenRepository;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class TokenService {
    private final TokenRepository tokenRepository;

    private final String jwtSecret;

    private final Long tokenExpLength;

    public TokenService(TokenRepository tokenRepository,
                        @Value("${bpm.jwt.secret}") String jwtSecret,
                        @Value("${bpm.jwt.exp.length}") Long tokenExpLength) {
        this.tokenRepository = tokenRepository;
        this.jwtSecret = jwtSecret;
        this.tokenExpLength = tokenExpLength;
    }

    public TokenEntity getById(String tokenId) {
        return tokenRepository.findById(tokenId).orElseThrow(() -> {
            throw new NotFoundCustomApplicationException(String.format("Token with ID = %d not found", tokenId));
        });
    }

    @Transactional
    public String generateAccessToken(UserEntity user) {
        LocalDateTime tokenExpDateTime = LocalDateTime.now().plus(tokenExpLength, ChronoUnit.MINUTES);
        Date date = Date.from(tokenExpDateTime.toInstant(ZonedDateTime.now().getOffset()));
        String token = Jwts.builder()
                .setSubject(String.valueOf(user.getLogin()))
                .claim("UserRoles", user.getRoles().stream().map(RoleEntity::getName).collect(Collectors.toSet()))
                .setAudience(TokenType.ACCESS.name())
                .setExpiration(date)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
        TokenEntity tokenForSave = TokenEntity.builder()
                .id(token)
                .expDateTime(tokenExpDateTime)
                .disabled(false)
                .user(user)
                .build();
        tokenRepository.save(tokenForSave);
        return token;
    }

    @Transactional
    public String generateRefreshToken(UserEntity user) {
        LocalDateTime tokenExpDateTime = LocalDate.now().plusDays(15).atTime(LocalTime.now());
        Date date = Date.from(tokenExpDateTime.toInstant(ZonedDateTime.now().getOffset()));
        String token = Jwts.builder()
                .setSubject(String.valueOf(user.getLogin()))
                .setAudience(TokenType.REFRESH.name())
                .setExpiration(date)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
        TokenEntity tokenForSave = TokenEntity.builder()
                .id(token)
                .expDateTime(tokenExpDateTime)
                .disabled(false)
                .user(user)
                .build();
        tokenRepository.save(tokenForSave);
        return token;
    }

    @Transactional
    public void disableByUserId (Long userId) {
        tokenRepository.setDisableByUserId(userId);
    }

    public Claims getValidAccessTokenClaims(String token) {
        return getValidTokenClaims(token, TokenType.ACCESS);
    }

    public Claims getValidRefreshTokenClaims(String token) {
        return getValidTokenClaims(token, TokenType.REFRESH);
    }

    public String getUserLoginFromToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    private boolean validateTokenInDb(String token) {
        TokenEntity tokenEntity = tokenRepository.findById(token).orElse(null);
        return tokenEntity != null && !tokenEntity.getDisabled() && !tokenEntity.getUser().getDeleted();
    }

    private Claims getValidTokenClaims(String token, TokenType tokenType) {
        try {
            Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
            if (tokenType.equals(TokenType.valueOf(claims.getAudience())) && validateTokenInDb(token)) {
                return claims;
            }
        } catch (ExpiredJwtException e) {
            throw new AuthenticationApplicationException("Token expired", e);
        } catch (UnsupportedJwtException e) {
            throw new AuthenticationApplicationException("Unsupported jwt", e);
        } catch (MalformedJwtException e) {
            throw new AuthenticationApplicationException("Malformed jwt", e);
        } catch (SignatureException e) {
            throw new AuthenticationApplicationException("Invalid signature", e);
        } catch (Exception e) {
            throw new AuthenticationApplicationException("Invalid token", e);
        }
        throw new AuthenticationApplicationException("Wrong type of token or disabled token.");
    }

}
