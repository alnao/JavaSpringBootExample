package it.alnao.springbootexample.aws.service.auth;

import it.alnao.springbootexample.port.config.SecurityConfig;
import it.alnao.springbootexample.port.domain.auth.RefreshToken;
import it.alnao.springbootexample.port.domain.auth.User;
import it.alnao.springbootexample.port.repository.auth.RefreshTokenRepository;
import it.alnao.springbootexample.port.service.auth.JwtService;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementazione AWS del service JWT.
 */
@Service
@Profile("aws")
public class JwtServiceImpl implements JwtService {
    
    private final SecurityConfig.JwtConfig jwtConfig;
    private final RefreshTokenRepository refreshTokenRepository;

    @Autowired
    public JwtServiceImpl(SecurityConfig.JwtConfig jwtConfig, RefreshTokenRepository refreshTokenRepository) {
        this.jwtConfig = jwtConfig;
        this.refreshTokenRepository = refreshTokenRepository;
    }
    
    @Override
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole().getRoleName());
        claims.put("accountType", user.getAccountType().name());
        
        return createToken(claims, user.getUsername() != null ? user.getUsername() : user.getEmail());
    }
    
    @Override
    @Transactional
    public RefreshToken generateRefreshToken(User user) {
        String token = UUID.randomUUID().toString();
        
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(UUID.randomUUID().toString());
        refreshToken.setToken(token);
        refreshToken.setUserId(user.getId());
    refreshToken.setExpiryDate(LocalDateTime.now().plusSeconds(jwtConfig.getRefreshExpiration()));
        refreshToken.setCreatedAt(LocalDateTime.now());
        
        return refreshTokenRepository.save(refreshToken);
    }
    

    @Override
    public boolean validateToken(String token, User user) {
        try {
            final String username = getUsernameFromToken(token);
            final String userIdentifier = user.getUsername() != null ? user.getUsername() : user.getEmail();
            return (username.equals(userIdentifier) && !isTokenExpired(token));
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
    public boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
    
    @Override
    @Transactional
    public Optional<String> refreshToken(String refreshTokenStr) {
        return refreshTokenRepository.findByToken(refreshTokenStr)
                .filter(refreshToken -> !refreshToken.isExpired())
                .map(refreshToken -> {
                    refreshToken.setLastUsed(LocalDateTime.now());
                    refreshTokenRepository.save(refreshToken);
                    return refreshTokenStr; // This should be a new JWT token
                });
    }
    
    @Override
    @Transactional
    public void invalidateRefreshToken(String refreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken);
    }
    
    @Override
    @Transactional
    public void invalidateAllUserTokens(String userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
    
    @Override
    @Transactional
    public void cleanExpiredTokens() {
        refreshTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }
    
    // Private helper methods
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
