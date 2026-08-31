package it.alnao.springbootexample.sqlite.repository.auth;

import it.alnao.springbootexample.core.domain.auth.UserProvider;
import it.alnao.springbootexample.sqlite.entity.auth.UserProviderSQLiteEntity;
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

class UserProviderRepositoryImplTest {

    @Mock UserProviderSQLiteRepository userProviderSQLiteRepository;
    @InjectMocks UserProviderRepositoryImpl repository;

    private UserProviderSQLiteEntity googleOfUser1;
    private UserProviderSQLiteEntity githubOfUser1;
    private UserProviderSQLiteEntity googleOfUser2;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        googleOfUser1 = entity("p1", "u1", "google", "g-1");
        githubOfUser1 = entity("p2", "u1", "github", "h-1");
        googleOfUser2 = entity("p3", "u2", "google", "g-2");
    }

    private UserProviderSQLiteEntity entity(String id, String userId, String provider, String externalId) {
        UserProviderSQLiteEntity e = new UserProviderSQLiteEntity();
        e.setId(id);
        e.setUserId(userId);
        e.setProvider(provider);
        e.setExternalId(externalId);
        return e;
    }

    @Test
    void save_mapsDomainToEntityAndBack() {
        UserProvider provider = new UserProvider("u1", "google", "g-1");
        when(userProviderSQLiteRepository.save(any(UserProviderSQLiteEntity.class))).thenReturn(googleOfUser1);
        UserProvider saved = repository.save(provider);
        assertEquals("google", saved.getProvider());
        assertEquals("g-1", saved.getProviderUserId());
        verify(userProviderSQLiteRepository).save(any(UserProviderSQLiteEntity.class));
    }

    @Test
    void findById_whenPresent_mapsToDomain() {
        when(userProviderSQLiteRepository.findById("p1")).thenReturn(Optional.of(googleOfUser1));
        Optional<UserProvider> result = repository.findById("p1");
        assertTrue(result.isPresent());
        assertEquals("u1", result.get().getUserId());
    }

    @Test
    void findById_whenMissing_returnsEmpty() {
        when(userProviderSQLiteRepository.findById("nope")).thenReturn(Optional.empty());
        assertTrue(repository.findById("nope").isEmpty());
    }

    @Test
    void findAll_mapsEveryEntity() {
        when(userProviderSQLiteRepository.findAll()).thenReturn(List.of(googleOfUser1, githubOfUser1));
        assertEquals(2, repository.findAll().size());
    }

    @Test
    void findByUserId_filtersOnUser() {
        when(userProviderSQLiteRepository.findAll())
                .thenReturn(List.of(googleOfUser1, githubOfUser1, googleOfUser2));
        assertEquals(2, repository.findByUserId("u1").size());
        assertEquals(1, repository.findByUserId("u2").size());
        assertTrue(repository.findByUserId("sconosciuto").isEmpty());
    }

    @Test
    void findByUserIdAndProvider_returnsMatchingProvider() {
        when(userProviderSQLiteRepository.findAll())
                .thenReturn(List.of(googleOfUser1, githubOfUser1, googleOfUser2));
        Optional<UserProvider> result = repository.findByUserIdAndProvider("u1", "github");
        assertTrue(result.isPresent());
        assertEquals("h-1", result.get().getProviderUserId());
        assertTrue(repository.findByUserIdAndProvider("u1", "facebook").isEmpty());
    }

    @Test
    void findByProviderAndProviderUserId_matchesBothFields() {
        when(userProviderSQLiteRepository.findAll())
                .thenReturn(List.of(googleOfUser1, githubOfUser1, googleOfUser2));
        Optional<UserProvider> result = repository.findByProviderAndProviderUserId("google", "g-2");
        assertTrue(result.isPresent());
        assertEquals("u2", result.get().getUserId());
        assertTrue(repository.findByProviderAndProviderUserId("google", "h-1").isEmpty());
    }

    @Test
    void existsByUserIdAndProvider_reflectsLookup() {
        when(userProviderSQLiteRepository.findAll()).thenReturn(List.of(googleOfUser1, githubOfUser1));
        assertTrue(repository.existsByUserIdAndProvider("u1", "google"));
        assertFalse(repository.existsByUserIdAndProvider("u1", "microsoft"));
    }

    @Test
    void deleteById_delegates() {
        repository.deleteById("p1");
        verify(userProviderSQLiteRepository).deleteById("p1");
    }

    @Test
    void deleteByUserId_deletesEveryProviderOfThatUser() {
        when(userProviderSQLiteRepository.findAll())
                .thenReturn(List.of(googleOfUser1, githubOfUser1, googleOfUser2));
        repository.deleteByUserId("u1");
        verify(userProviderSQLiteRepository).deleteById("p1");
        verify(userProviderSQLiteRepository).deleteById("p2");
        verify(userProviderSQLiteRepository, never()).deleteById("p3");
    }

    @Test
    void deleteByUserIdAndProvider_deletesOnlyThatOne() {
        when(userProviderSQLiteRepository.findAll())
                .thenReturn(List.of(googleOfUser1, githubOfUser1));
        repository.deleteByUserIdAndProvider("u1", "github");
        verify(userProviderSQLiteRepository).deleteById("p2");
        verify(userProviderSQLiteRepository, never()).deleteById("p1");
    }

    @Test
    void deleteByUserIdAndProvider_whenAbsent_deletesNothing() {
        when(userProviderSQLiteRepository.findAll()).thenReturn(List.of(googleOfUser1));
        repository.deleteByUserIdAndProvider("u1", "facebook");
        verify(userProviderSQLiteRepository, never()).deleteById(anyString());
    }
}
