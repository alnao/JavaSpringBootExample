package it.alnao.springbootexample.azure.repository.auth;

import it.alnao.springbootexample.azure.entity.auth.RefreshTokenSqlServerEntity;
import it.alnao.springbootexample.core.domain.auth.RefreshToken;
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

class RefreshTokenRepositoryAzureImplTest {

    @Mock RefreshTokenSqlServerRepository jpaRepository;
    @InjectMocks RefreshTokenRepositoryAzureImpl repository;

    private RefreshTokenSqlServerEntity scaduto;
    private RefreshTokenSqlServerEntity valido;
    private RefreshTokenSqlServerEntity altroUtente;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        scaduto = entity("t1", "token-1", "u1", LocalDateTime.now().minusDays(1));
        valido = entity("t2", "token-2", "u1", LocalDateTime.now().plusDays(1));
        altroUtente = entity("t3", "token-3", "u2", LocalDateTime.now().plusDays(1));
    }

    private RefreshTokenSqlServerEntity entity(String id, String token, String userId, LocalDateTime expiry) {
        RefreshToken domain = new RefreshToken();
        domain.setId(id);
        domain.setToken(token);
        domain.setUserId(userId);
        domain.setExpiryDate(expiry);
        return RefreshTokenSqlServerEntity.fromDomain(domain);
    }

    @Test
    void findAll_mapsEveryEntity() {
        when(jpaRepository.findAll()).thenReturn(List.of(scaduto, valido));
        assertEquals(2, repository.findAll().size());
    }

    @Test
    void save_generatesAnIdWhenMissing() {
        RefreshToken domain = new RefreshToken();
        domain.setToken("nuovo");
        domain.setUserId("u1");
        domain.setExpiryDate(LocalDateTime.now().plusDays(1));
        when(jpaRepository.save(any(RefreshTokenSqlServerEntity.class))).thenReturn(valido);

        repository.save(domain);

        assertNotNull(domain.getId());
    }

    @Test
    void save_keepsAnExistingId() {
        RefreshToken domain = new RefreshToken();
        domain.setId("id-esistente");
        domain.setToken("nuovo");
        domain.setUserId("u1");
        domain.setExpiryDate(LocalDateTime.now().plusDays(1));
        when(jpaRepository.save(any(RefreshTokenSqlServerEntity.class))).thenReturn(valido);

        repository.save(domain);

        assertEquals("id-esistente", domain.getId());
    }

    @Test
    void findByToken_whenPresent_mapsToDomain() {
        when(jpaRepository.findByToken("token-2")).thenReturn(Optional.of(valido));
        Optional<RefreshToken> result = repository.findByToken("token-2");
        assertTrue(result.isPresent());
        assertEquals("u1", result.get().getUserId());
    }

    @Test
    void findByToken_whenMissing_returnsEmpty() {
        when(jpaRepository.findByToken("assente")).thenReturn(Optional.empty());
        assertTrue(repository.findByToken("assente").isEmpty());
    }

    @Test
    void findByUserId_filtersOnUser() {
        when(jpaRepository.findAll()).thenReturn(List.of(scaduto, valido, altroUtente));
        assertEquals(2, repository.findByUserId("u1").size());
        assertEquals(1, repository.findByUserId("u2").size());
    }

    @Test
    void delete_usesTheTokenId() {
        RefreshToken domain = new RefreshToken();
        domain.setId("t2");
        repository.delete(domain);
        verify(jpaRepository).deleteById("t2");
    }

    @Test
    void deleteByToken_whenPresent_deletesIt() {
        when(jpaRepository.findByToken("token-2")).thenReturn(Optional.of(valido));
        repository.deleteByToken("token-2");
        verify(jpaRepository).deleteById("t2");
    }

    @Test
    void deleteByToken_whenMissing_deletesNothing() {
        when(jpaRepository.findByToken("assente")).thenReturn(Optional.empty());
        repository.deleteByToken("assente");
        verify(jpaRepository, never()).deleteById(anyString());
    }

    @Test
    void deleteByUserId_deletesEveryTokenOfThatUser() {
        when(jpaRepository.findAll()).thenReturn(List.of(scaduto, valido, altroUtente));
        repository.deleteByUserId("u1");
        verify(jpaRepository).deleteById("t1");
        verify(jpaRepository).deleteById("t2");
        verify(jpaRepository, never()).deleteById("t3");
    }

    @Test
    void deleteByExpiryDateBefore_deletesOnlyTheExpiredOnes() {
        when(jpaRepository.findAll()).thenReturn(List.of(scaduto, valido, altroUtente));
        repository.deleteByExpiryDateBefore(LocalDateTime.now());
        verify(jpaRepository).deleteById("t1");
        verify(jpaRepository, never()).deleteById("t2");
    }

    @Test
    void deleteByExpiryDateBefore_ignoresRowsWithoutExpiry() {
        RefreshTokenSqlServerEntity senzaScadenza = entity("t4", "token-4", "u1", null);
        when(jpaRepository.findAll()).thenReturn(List.of(senzaScadenza));
        repository.deleteByExpiryDateBefore(LocalDateTime.now());
        verify(jpaRepository, never()).deleteById(anyString());
    }
}
