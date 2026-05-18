package it.alnao.springbootexample.core.service.auth.impl;

import it.alnao.springbootexample.core.config.JwtConfig;
import it.alnao.springbootexample.core.domain.auth.AccountType;
import it.alnao.springbootexample.core.domain.auth.User;
import it.alnao.springbootexample.core.domain.auth.UserRole;
import it.alnao.springbootexample.core.repository.auth.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class JwtServiceDefaultTest {

    private static final String SECRET = "mySecretKeyForTestingPurposesAtLeast64BytesLongAbcdefghijklmnopqrst";

    private JwtServiceDefault jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        JwtConfig.JwtConfigBean config = new JwtConfig.JwtConfigBean(SECRET, 3600L, 86400L);
        jwtService = new JwtServiceDefault(config, Optional.empty());

        user = new User();
        user.setId("user-id-1");
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setRole(UserRole.USER);
        user.setAccountType(AccountType.LOCAL);
    }

    // ---- generateToken ----

    @Test
    void generateToken_ritornaTokenNonNull() {
        String token = jwtService.generateToken(user);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void generateToken_usernamePrincipale_sePresente() {
        String token = jwtService.generateToken(user);
        String username = jwtService.getUsernameFromToken(token);
        assertEquals("testuser", username);
    }

    @Test
    void generateToken_usaEmailSeUsernameNull() {
        user.setUsername(null);
        String token = jwtService.generateToken(user);
        String username = jwtService.getUsernameFromToken(token);
        assertEquals("test@example.com", username);
    }

    // ---- validateToken ----

    @Test
    void validateToken_tokenValido_ritornaTrue() {
        String token = jwtService.generateToken(user);
        assertTrue(jwtService.validateToken(token, user));
    }

    @Test
    void validateToken_tokenPerAltroUtente_ritornaFalse() {
        User altroUser = new User();
        altroUser.setUsername("altrouser");
        altroUser.setEmail("altro@example.com");
        altroUser.setRole(UserRole.USER);
        altroUser.setAccountType(AccountType.LOCAL);

        String token = jwtService.generateToken(altroUser);
        assertFalse(jwtService.validateToken(token, user));
    }

    @Test
    void validateToken_tokenNonValido_ritornaFalse() {
        assertFalse(jwtService.validateToken("token.non.valido", user));
    }

    // ---- getUsernameFromToken ----

    @Test
    void getUsernameFromToken_ritornaUsernameCorretto() {
        String token = jwtService.generateToken(user);
        assertEquals("testuser", jwtService.getUsernameFromToken(token));
    }

    // ---- getUserIdFromToken ----

    @Test
    void getUserIdFromToken_ritornaIdCorretto() {
        String token = jwtService.generateToken(user);
        assertEquals("user-id-1", jwtService.getUserIdFromToken(token));
    }

    // ---- getRoleFromToken ----

    @Test
    void getRoleFromToken_ritornaRuoloCorretto() {
        String token = jwtService.generateToken(user);
        assertEquals("USER", jwtService.getRoleFromToken(token));
    }

    // ---- isTokenExpired ----

    @Test
    void isTokenExpired_tokenFresco_ritornaFalse() {
        String token = jwtService.generateToken(user);
        assertFalse(jwtService.isTokenExpired(token));
    }

    // ---- generateRefreshToken without repository ----

    @Test
    void generateRefreshToken_senzaRepository_lanciaUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () ->
                jwtService.generateRefreshToken(user));
    }

    // ---- refreshToken without repository ----

    @Test
    void refreshToken_senzaRepository_ritornaEmpty() {
        Optional<String> result = jwtService.refreshToken("sometoken");
        assertTrue(result.isEmpty());
    }

    // ---- invalidateRefreshToken / invalidateAllUserTokens / cleanExpiredTokens ----

    @Test
    void invalidateRefreshToken_senzaRepository_nonLanciaEccezione() {
        assertDoesNotThrow(() -> jwtService.invalidateRefreshToken("token"));
    }

    @Test
    void invalidateAllUserTokens_senzaRepository_nonLanciaEccezione() {
        assertDoesNotThrow(() -> jwtService.invalidateAllUserTokens("user-id"));
    }

    @Test
    void cleanExpiredTokens_senzaRepository_nonLanciaEccezione() {
        assertDoesNotThrow(() -> jwtService.cleanExpiredTokens());
    }
}
