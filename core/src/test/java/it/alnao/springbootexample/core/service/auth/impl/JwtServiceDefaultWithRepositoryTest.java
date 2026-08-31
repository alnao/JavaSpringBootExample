package it.alnao.springbootexample.core.service.auth.impl;

import it.alnao.springbootexample.core.config.JwtConfig;
import it.alnao.springbootexample.core.domain.auth.AccountType;
import it.alnao.springbootexample.core.domain.auth.RefreshToken;
import it.alnao.springbootexample.core.domain.auth.User;
import it.alnao.springbootexample.core.domain.auth.UserRole;
import it.alnao.springbootexample.core.repository.auth.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Copre i rami che richiedono un RefreshTokenRepository presente, complementari
 * ai casi "senza repository" gia' verificati in {@link JwtServiceDefaultTest}.
 */
class JwtServiceDefaultWithRepositoryTest {

    @Mock RefreshTokenRepository refreshTokenRepository;

    private JwtServiceDefault jwtService;
    private User user;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        JwtConfig.JwtConfigBean config = new JwtConfig.JwtConfigBean(
                "un-secret-di-test-abbastanza-lungo-per-hmac-sha256-1234567890", 3600L, 604800L);
        jwtService = new JwtServiceDefault(config, Optional.of(refreshTokenRepository));

        user = new User();
        user.setId("u-1");
        user.setUsername("mario");
        user.setEmail("mario@test.it");
        user.setRole(UserRole.USER);
        user.setAccountType(AccountType.LOCAL);
    }

    // ---------- generateRefreshToken ----------

    @Test
    void generateRefreshToken_persistsAFreshToken() {
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        RefreshToken result = jwtService.generateRefreshToken(user);

        assertNotNull(result.getId());
        assertNotNull(result.getToken());
        assertEquals("u-1", result.getUserId());
        assertNotNull(result.getCreatedAt());
        assertTrue(result.getExpiryDate().isAfter(LocalDateTime.now()));
    }

    @Test
    void generateRefreshToken_appliesTheConfiguredRefreshExpiration() {
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        jwtService.generateRefreshToken(user);

        verify(refreshTokenRepository).save(captor.capture());
        RefreshToken saved = captor.getValue();
        // 604800 secondi = 7 giorni
        assertTrue(saved.getExpiryDate().isAfter(LocalDateTime.now().plusDays(6)));
        assertTrue(saved.getExpiryDate().isBefore(LocalDateTime.now().plusDays(8)));
    }

    @Test
    void generateRefreshToken_producesADifferentTokenEveryTime() {
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));
        assertNotEquals(jwtService.generateRefreshToken(user).getToken(),
                        jwtService.generateRefreshToken(user).getToken());
    }

    // ---------- refreshToken ----------

    @Test
    void refreshToken_whenValid_stampsLastUsedAndReturnsTheToken() {
        RefreshToken stored = new RefreshToken("token-1", "u-1", LocalDateTime.now().plusDays(1));
        when(refreshTokenRepository.findByToken("token-1")).thenReturn(Optional.of(stored));

        Optional<String> result = jwtService.refreshToken("token-1");

        assertTrue(result.isPresent());
        assertEquals("token-1", result.get());
        assertNotNull(stored.getLastUsed());
        verify(refreshTokenRepository).save(stored);
    }

    @Test
    void refreshToken_whenExpired_returnsEmptyAndSavesNothing() {
        RefreshToken scaduto = new RefreshToken("token-scaduto", "u-1", LocalDateTime.now().minusDays(1));
        when(refreshTokenRepository.findByToken("token-scaduto")).thenReturn(Optional.of(scaduto));

        assertTrue(jwtService.refreshToken("token-scaduto").isEmpty());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void refreshToken_whenUnknown_returnsEmpty() {
        when(refreshTokenRepository.findByToken("sconosciuto")).thenReturn(Optional.empty());
        assertTrue(jwtService.refreshToken("sconosciuto").isEmpty());
    }

    // ---------- invalidazione ----------

    @Test
    void invalidateRefreshToken_delegatesToTheRepository() {
        jwtService.invalidateRefreshToken("token-1");
        verify(refreshTokenRepository).deleteByToken("token-1");
    }

    @Test
    void invalidateAllUserTokens_delegatesToTheRepository() {
        jwtService.invalidateAllUserTokens("u-1");
        verify(refreshTokenRepository).deleteByUserId("u-1");
    }

    @Test
    void cleanExpiredTokens_deletesEverythingExpiredBeforeNow() {
        jwtService.cleanExpiredTokens();
        verify(refreshTokenRepository).deleteByExpiryDateBefore(any(LocalDateTime.class));
    }
}
