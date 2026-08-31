package it.alnao.springbootexample.aws.repository.auth;

import it.alnao.springbootexample.aws.entity.auth.RefreshTokenMySQLEntity;
import it.alnao.springbootexample.aws.entity.auth.UserProviderMySQLEntity;
import it.alnao.springbootexample.core.domain.auth.RefreshToken;
import it.alnao.springbootexample.core.domain.auth.UserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthRepositoryImplTest {

    @Mock UserProviderMySQLJpaRepository providerJpaRepository;
    @Mock RefreshTokenMySQLJpaRepository tokenJpaRepository;

    @InjectMocks UserProviderRepositoryImpl providerRepository;
    @InjectMocks RefreshTokenRepositoryImpl tokenRepository;

    private UserProviderMySQLEntity providerEntity;
    private RefreshTokenMySQLEntity tokenEntity;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        UserProvider provider = new UserProvider("u-1", "google", "g-1");
        provider.setId("p-1");
        providerEntity = UserProviderMySQLEntity.fromDomain(provider);

        RefreshToken token = new RefreshToken("token-1", "u-1", LocalDateTime.now().plusDays(1));
        token.setId("t-1");
        tokenEntity = RefreshTokenMySQLEntity.fromDomain(token);
    }

    // ---------- UserProviderRepositoryImpl ----------

    @Test
    void provider_saveGeneratesAnIdWhenMissing() {
        UserProvider domain = new UserProvider("u-1", "google", "g-1");
        when(providerJpaRepository.save(any(UserProviderMySQLEntity.class))).thenReturn(providerEntity);
        providerRepository.save(domain);
        assertNotNull(domain.getId());
    }

    @Test
    void provider_saveKeepsAnExistingId() {
        UserProvider domain = new UserProvider("u-1", "google", "g-1");
        domain.setId("gia-presente");
        when(providerJpaRepository.save(any(UserProviderMySQLEntity.class))).thenReturn(providerEntity);
        providerRepository.save(domain);
        assertEquals("gia-presente", domain.getId());
    }

    @Test
    void provider_findByIdMapsToDomain() {
        when(providerJpaRepository.findById("p-1")).thenReturn(Optional.of(providerEntity));
        Optional<UserProvider> result = providerRepository.findById("p-1");
        assertTrue(result.isPresent());
        assertEquals("u-1", result.get().getUserId());
    }

    @Test
    void provider_findByIdWhenMissingReturnsEmpty() {
        when(providerJpaRepository.findById("nope")).thenReturn(Optional.empty());
        assertTrue(providerRepository.findById("nope").isEmpty());
    }

    @Test
    void provider_findByUserIdDelegates() {
        when(providerJpaRepository.findByUserId("u-1")).thenReturn(List.of(providerEntity));
        assertEquals(1, providerRepository.findByUserId("u-1").size());
    }

    @Test
    void provider_findByUserIdAndProviderDelegates() {
        when(providerJpaRepository.findByUserIdAndProvider("u-1", "google"))
                .thenReturn(Optional.of(providerEntity));
        assertTrue(providerRepository.findByUserIdAndProvider("u-1", "google").isPresent());
    }

    @Test
    void provider_findByProviderAndProviderUserIdDelegates() {
        when(providerJpaRepository.findByProviderAndProviderUserId("google", "g-1"))
                .thenReturn(Optional.of(providerEntity));
        assertTrue(providerRepository.findByProviderAndProviderUserId("google", "g-1").isPresent());
    }

    @Test
    void provider_existsByUserIdAndProviderDelegates() {
        when(providerJpaRepository.existsByUserIdAndProvider("u-1", "google")).thenReturn(true);
        assertTrue(providerRepository.existsByUserIdAndProvider("u-1", "google"));
    }

    @Test
    void provider_findAllMapsEveryEntity() {
        when(providerJpaRepository.findAll()).thenReturn(List.of(providerEntity, providerEntity));
        assertEquals(2, providerRepository.findAll().size());
    }

    @Test
    void provider_deleteMethodsDelegate() {
        providerRepository.deleteById("p-1");
        providerRepository.deleteByUserId("u-1");
        providerRepository.deleteByUserIdAndProvider("u-1", "google");
        verify(providerJpaRepository).deleteById("p-1");
        verify(providerJpaRepository).deleteByUserId("u-1");
        verify(providerJpaRepository).deleteByUserIdAndProvider("u-1", "google");
    }

    // ---------- RefreshTokenRepositoryImpl ----------

    @Test
    void token_saveGeneratesAnIdWhenMissing() {
        RefreshToken domain = new RefreshToken("tok", "u-1", LocalDateTime.now().plusDays(1));
        when(tokenJpaRepository.save(any(RefreshTokenMySQLEntity.class))).thenReturn(tokenEntity);
        tokenRepository.save(domain);
        assertNotNull(domain.getId());
    }

    @Test
    void token_saveKeepsAnExistingId() {
        RefreshToken domain = new RefreshToken("tok", "u-1", LocalDateTime.now().plusDays(1));
        domain.setId("gia-presente");
        when(tokenJpaRepository.save(any(RefreshTokenMySQLEntity.class))).thenReturn(tokenEntity);
        tokenRepository.save(domain);
        assertEquals("gia-presente", domain.getId());
    }

    @Test
    void token_findByTokenMapsToDomain() {
        when(tokenJpaRepository.findByToken("token-1")).thenReturn(Optional.of(tokenEntity));
        Optional<RefreshToken> result = tokenRepository.findByToken("token-1");
        assertTrue(result.isPresent());
        assertEquals("u-1", result.get().getUserId());
    }

    @Test
    void token_findByTokenWhenMissingReturnsEmpty() {
        when(tokenJpaRepository.findByToken("assente")).thenReturn(Optional.empty());
        assertTrue(tokenRepository.findByToken("assente").isEmpty());
    }

    @Test
    void token_findByUserIdDelegates() {
        when(tokenJpaRepository.findByUserId("u-1")).thenReturn(List.of(tokenEntity));
        assertEquals(1, tokenRepository.findByUserId("u-1").size());
    }

    @Test
    void token_findAllMapsEveryEntity() {
        when(tokenJpaRepository.findAll()).thenReturn(List.of(tokenEntity, tokenEntity));
        assertEquals(2, tokenRepository.findAll().size());
    }

    @Test
    void token_deleteUsesTheTokenId() {
        RefreshToken domain = new RefreshToken();
        domain.setId("t-1");
        tokenRepository.delete(domain);
        verify(tokenJpaRepository).deleteById("t-1");
    }

    @Test
    void token_deleteMethodsDelegate() {
        LocalDateTime soglia = LocalDateTime.now();
        tokenRepository.deleteByToken("token-1");
        tokenRepository.deleteByUserId("u-1");
        tokenRepository.deleteByExpiryDateBefore(soglia);
        verify(tokenJpaRepository).deleteByToken("token-1");
        verify(tokenJpaRepository).deleteByUserId("u-1");
        verify(tokenJpaRepository).deleteByExpiryDateBefore(soglia);
    }
}
