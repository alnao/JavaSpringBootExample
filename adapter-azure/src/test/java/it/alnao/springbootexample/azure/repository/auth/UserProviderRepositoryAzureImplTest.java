package it.alnao.springbootexample.azure.repository.auth;

import it.alnao.springbootexample.azure.entity.auth.UserProviderSqlServerEntity;
import it.alnao.springbootexample.core.domain.auth.UserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserProviderRepositoryAzureImplTest {

    @Mock UserProviderSqlServerRepository jpaRepository;
    @InjectMocks UserProviderRepositoryAzureImpl repository;

    private UserProviderSqlServerEntity googleOfUser1;
    private UserProviderSqlServerEntity githubOfUser1;
    private UserProviderSqlServerEntity googleOfUser2;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        googleOfUser1 = entity("p1", "u1", "google", "g-1");
        githubOfUser1 = entity("p2", "u1", "github", "h-1");
        googleOfUser2 = entity("p3", "u2", "google", "g-2");
    }

    private UserProviderSqlServerEntity entity(String id, String userId, String provider, String providerUserId) {
        UserProvider domain = new UserProvider(userId, provider, providerUserId);
        domain.setId(id);
        return UserProviderSqlServerEntity.fromDomain(domain);
    }

    @Test
    void findAll_mapsEveryEntity() {
        when(jpaRepository.findAll()).thenReturn(List.of(googleOfUser1, githubOfUser1));
        assertEquals(2, repository.findAll().size());
    }

    @Test
    void save_generatesAnIdWhenMissing() {
        UserProvider domain = new UserProvider("u1", "google", "g-1");
        assertNull(domain.getId());
        when(jpaRepository.save(any(UserProviderSqlServerEntity.class))).thenReturn(googleOfUser1);

        repository.save(domain);

        assertNotNull(domain.getId());
        verify(jpaRepository).save(any(UserProviderSqlServerEntity.class));
    }

    @Test
    void save_keepsAnExistingId() {
        UserProvider domain = new UserProvider("u1", "google", "g-1");
        domain.setId("id-esistente");
        when(jpaRepository.save(any(UserProviderSqlServerEntity.class))).thenReturn(googleOfUser1);

        repository.save(domain);

        assertEquals("id-esistente", domain.getId());
    }

    @Test
    void findById_whenPresent_mapsToDomain() {
        when(jpaRepository.findById("p1")).thenReturn(Optional.of(googleOfUser1));
        Optional<UserProvider> result = repository.findById("p1");
        assertTrue(result.isPresent());
        assertEquals("u1", result.get().getUserId());
    }

    @Test
    void findById_whenMissing_returnsEmpty() {
        when(jpaRepository.findById("nope")).thenReturn(Optional.empty());
        assertTrue(repository.findById("nope").isEmpty());
    }

    @Test
    void findByUserId_delegatesToTheDerivedQuery() {
        when(jpaRepository.findByUserId("u1")).thenReturn(List.of(googleOfUser1, githubOfUser1));
        assertEquals(2, repository.findByUserId("u1").size());
        verify(jpaRepository).findByUserId("u1");
    }

    @Test
    void findByUserIdAndProvider_returnsTheMatchingOne() {
        when(jpaRepository.findAll()).thenReturn(List.of(googleOfUser1, githubOfUser1, googleOfUser2));
        Optional<UserProvider> result = repository.findByUserIdAndProvider("u1", "github");
        assertTrue(result.isPresent());
        assertEquals("h-1", result.get().getProviderUserId());
    }

    @Test
    void findByUserIdAndProvider_whenAbsent_returnsEmpty() {
        when(jpaRepository.findAll()).thenReturn(List.of(googleOfUser1));
        assertTrue(repository.findByUserIdAndProvider("u1", "facebook").isEmpty());
    }

    @Test
    void findByProviderAndProviderUserId_delegatesToTheDerivedQuery() {
        when(jpaRepository.findByProviderAndProviderUserId("google", "g-1"))
                .thenReturn(Optional.of(googleOfUser1));
        assertTrue(repository.findByProviderAndProviderUserId("google", "g-1").isPresent());
    }

    @Test
    void existsByUserIdAndProvider_reflectsTheStoredRows() {
        when(jpaRepository.findAll()).thenReturn(List.of(googleOfUser1, githubOfUser1));
        assertTrue(repository.existsByUserIdAndProvider("u1", "google"));
        assertFalse(repository.existsByUserIdAndProvider("u2", "google"));
    }

    @Test
    void deleteById_delegates() {
        repository.deleteById("p1");
        verify(jpaRepository).deleteById("p1");
    }

    @Test
    void deleteByUserId_deletesEveryProviderOfThatUser() {
        when(jpaRepository.findAll()).thenReturn(List.of(googleOfUser1, githubOfUser1, googleOfUser2));
        repository.deleteByUserId("u1");
        verify(jpaRepository).deleteById("p1");
        verify(jpaRepository).deleteById("p2");
        verify(jpaRepository, never()).deleteById("p3");
    }

    @Test
    void deleteByUserIdAndProvider_deletesOnlyThatCombination() {
        when(jpaRepository.findAll()).thenReturn(List.of(googleOfUser1, githubOfUser1, googleOfUser2));
        repository.deleteByUserIdAndProvider("u1", "google");
        verify(jpaRepository).deleteById("p1");
        verify(jpaRepository, never()).deleteById("p2");
        verify(jpaRepository, never()).deleteById("p3");
    }
}
