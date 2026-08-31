package it.alnao.springbootexample.sqlite.repository.auth;

import it.alnao.springbootexample.core.domain.auth.AccountType;
import it.alnao.springbootexample.core.domain.auth.User;
import it.alnao.springbootexample.sqlite.entity.auth.UserSQLiteEntity;
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

class UserRepositoryImplTest {

    @Mock UserSQLiteRepository userSQLiteRepository;
    @InjectMocks UserRepositoryImpl repository;

    private UserSQLiteEntity localUser;
    private UserSQLiteEntity googleUser;
    private UserSQLiteEntity disabledUser;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        localUser = entity("1", "mario", "mario@test.it", AccountType.LOCAL, "ext-1", true);
        googleUser = entity("2", "luigi", "luigi@test.it", AccountType.GOOGLE, "ext-2", true);
        disabledUser = entity("3", "peach", "peach@test.it", AccountType.LOCAL, "ext-3", false);
    }

    private UserSQLiteEntity entity(String id, String username, String email,
                                    AccountType type, String externalId, boolean enabled) {
        UserSQLiteEntity e = new UserSQLiteEntity();
        e.setId(id);
        e.setUsername(username);
        e.setEmail(email);
        e.setAccountType(type);
        e.setExternalId(externalId);
        e.setEnabled(enabled);
        return e;
    }

    @Test
    void findByExternalIdAndAccountType_whenMatching_returnsUser() {
        when(userSQLiteRepository.findAll()).thenReturn(List.of(localUser, googleUser));
        Optional<User> result = repository.findByExternalIdAndAccountType("ext-2", AccountType.GOOGLE);
        assertTrue(result.isPresent());
        assertEquals("luigi", result.get().getUsername());
    }

    @Test
    void findByExternalIdAndAccountType_whenTypeDiffers_returnsEmpty() {
        when(userSQLiteRepository.findAll()).thenReturn(List.of(localUser, googleUser));
        assertTrue(repository.findByExternalIdAndAccountType("ext-2", AccountType.GITHUB).isEmpty());
    }

    @Test
    void existsByEmail_reflectsRepositoryLookup() {
        when(userSQLiteRepository.findByEmail("mario@test.it")).thenReturn(Optional.of(localUser));
        when(userSQLiteRepository.findByEmail("nope@test.it")).thenReturn(Optional.empty());
        assertTrue(repository.existsByEmail("mario@test.it"));
        assertFalse(repository.existsByEmail("nope@test.it"));
    }

    @Test
    void findByAccountType_filtersByType() {
        when(userSQLiteRepository.findAll()).thenReturn(List.of(localUser, googleUser, disabledUser));
        List<User> locals = repository.findByAccountType(AccountType.LOCAL);
        assertEquals(2, locals.size());
        assertEquals(1, repository.findByAccountType(AccountType.GOOGLE).size());
    }

    @Test
    void save_mapsDomainToEntityAndBack() {
        User user = new User("mario", "mario@test.it", "pwd");
        when(userSQLiteRepository.save(any(UserSQLiteEntity.class))).thenReturn(localUser);
        User saved = repository.save(user);
        assertEquals("mario", saved.getUsername());
        verify(userSQLiteRepository).save(any(UserSQLiteEntity.class));
    }

    @Test
    void findByEmailAndAccountType_matchesBothFields() {
        when(userSQLiteRepository.findAll()).thenReturn(List.of(localUser, googleUser));
        assertTrue(repository.findByEmailAndAccountType("mario@test.it", AccountType.LOCAL).isPresent());
        assertTrue(repository.findByEmailAndAccountType("mario@test.it", AccountType.GOOGLE).isEmpty());
    }

    @Test
    void findById_delegatesAndMaps() {
        when(userSQLiteRepository.findById("1")).thenReturn(Optional.of(localUser));
        Optional<User> result = repository.findById("1");
        assertTrue(result.isPresent());
        assertEquals("1", result.get().getId());
    }

    @Test
    void findById_whenMissing_returnsEmpty() {
        when(userSQLiteRepository.findById("x")).thenReturn(Optional.empty());
        assertTrue(repository.findById("x").isEmpty());
    }

    @Test
    void findByUsername_delegatesAndMaps() {
        when(userSQLiteRepository.findByUsername("mario")).thenReturn(Optional.of(localUser));
        assertTrue(repository.findByUsername("mario").isPresent());
    }

    @Test
    void findByEmail_delegatesAndMaps() {
        when(userSQLiteRepository.findByEmail("mario@test.it")).thenReturn(Optional.of(localUser));
        assertTrue(repository.findByEmail("mario@test.it").isPresent());
    }

    @Test
    void existsByUsername_reflectsRepositoryLookup() {
        when(userSQLiteRepository.findByUsername("mario")).thenReturn(Optional.of(localUser));
        when(userSQLiteRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        assertTrue(repository.existsByUsername("mario"));
        assertFalse(repository.existsByUsername("ghost"));
    }

    @Test
    void findAll_mapsEveryEntity() {
        when(userSQLiteRepository.findAll()).thenReturn(List.of(localUser, googleUser));
        assertEquals(2, repository.findAll().size());
    }

    @Test
    void findByEnabled_filtersOnFlag() {
        when(userSQLiteRepository.findAll()).thenReturn(List.of(localUser, googleUser, disabledUser));
        assertEquals(2, repository.findByEnabled(true).size());
        assertEquals(1, repository.findByEnabled(false).size());
    }

    @Test
    void deleteById_delegates() {
        repository.deleteById("1");
        verify(userSQLiteRepository).deleteById("1");
    }

    @Test
    void delete_usesUserId() {
        User user = new User();
        user.setId("42");
        repository.delete(user);
        verify(userSQLiteRepository).deleteById("42");
    }

    @Test
    void count_delegates() {
        when(userSQLiteRepository.count()).thenReturn(7L);
        assertEquals(7L, repository.count());
    }

    @Test
    void countByAccountType_countsMatchingOnly() {
        when(userSQLiteRepository.findAll()).thenReturn(List.of(localUser, googleUser, disabledUser));
        assertEquals(2L, repository.countByAccountType(AccountType.LOCAL));
        assertEquals(0L, repository.countByAccountType(AccountType.GITHUB));
    }
}
