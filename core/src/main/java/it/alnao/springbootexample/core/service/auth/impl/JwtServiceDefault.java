package it.alnao.springbootexample.core.service.auth.impl;

import it.alnao.springbootexample.core.config.SecurityConfig;
import it.alnao.springbootexample.core.domain.auth.RefreshToken;
import it.alnao.springbootexample.core.domain.auth.User;
import it.alnao.springbootexample.core.repository.auth.RefreshTokenRepository;
import it.alnao.springbootexample.core.service.auth.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Default, profile-agnostic implementation of JwtService living in core.
 * Uses SecurityConfig.JwtConfig for secret/expiration and optionally persists refresh tokens
 * if a RefreshTokenRepository is available.
 */
@Service
@Primary
public class JwtServiceDefault implements JwtService {

    private final SecurityConfig.JwtConfig jwtConfig;
    private final Optional<RefreshTokenRepository> refreshTokenRepository;

    @Autowired
    public JwtServiceDefault(SecurityConfig.JwtConfig jwtConfig, Optional<RefreshTokenRepository> refreshTokenRepository) {
        this.jwtConfig = jwtConfig;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole().getRoleName());
        claims.put("accountType", user.getAccountType().name());

        String subject = user.getUsername() != null ? user.getUsername() : user.getEmail();
        return createToken(claims, subject);
    }

    @Override
    @Transactional
    public RefreshToken generateRefreshToken(User user) {
        RefreshTokenRepository repo = refreshTokenRepository
                .orElseThrow(() -> new UnsupportedOperationException("Refresh tokens repository not available for current profile"));

        String token = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(UUID.randomUUID().toString());
        refreshToken.setToken(token);
        refreshToken.setUserId(user.getId());
        refreshToken.setExpiryDate(LocalDateTime.now().plusSeconds(jwtConfig.getRefreshExpiration()));
        refreshToken.setCreatedAt(LocalDateTime.now());
        return repo.save(refreshToken);
    }

    @Override
    public boolean validateToken(String token, User user) {
        try {
            final String username = getUsernameFromToken(token);
            final String userIdentifier = user.getUsername() != null ? user.getUsername() : user.getEmail();
            return (Objects.equals(username, userIdentifier) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    @Override
    public String getUserIdFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("userId", String.class));
    }

    @Override
    public String getRoleFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("role", String.class));
    }

    @Override
    public boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    @Override
    @Transactional
    public Optional<String> refreshToken(String refreshTokenStr) {
        return refreshTokenRepository
                .map(repo -> repo.findByToken(refreshTokenStr)
                        .filter(rt -> !rt.isExpired())
                        .map(rt -> {
                            rt.setLastUsed(LocalDateTime.now());
                            repo.save(rt);
                            // In a full implementation we'd issue a new JWT here, leaving as-is to match prior behavior
                            return refreshTokenStr;
                        })
                )
                .orElseGet(Optional::empty);
    }

    @Override
    @Transactional
    public void invalidateRefreshToken(String refreshToken) {
        refreshTokenRepository.ifPresent(repo -> repo.deleteByToken(refreshToken));
    }

    @Override
    @Transactional
    public void invalidateAllUserTokens(String userId) {
        refreshTokenRepository.ifPresent(repo -> repo.deleteByUserId(userId));
    }

    @Override
    @Transactional
    public void cleanExpiredTokens() {
        refreshTokenRepository.ifPresent(repo -> repo.deleteByExpiryDateBefore(LocalDateTime.now()));
    }

    // Helpers
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtConfig.getExpiration() * 1000))
                .signWith(getSignKey(), Jwts.SIG.HS512)
                .compact();
    }

    private SecretKey getSignKey() {
        byte[] keyBytes = jwtConfig.getSecret().getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private <T> T getClaimFromToken(String token, ClaimsResolver<T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.resolve(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    @FunctionalInterface
    private interface ClaimsResolver<T> {
        T resolve(Claims claims);
    }
}
