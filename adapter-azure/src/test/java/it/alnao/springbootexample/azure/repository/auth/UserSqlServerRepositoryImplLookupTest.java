package it.alnao.springbootexample.azure.repository.auth;

import it.alnao.springbootexample.azure.entity.auth.UserSqlServerEntity;
import it.alnao.springbootexample.core.domain.auth.AccountType;
import it.alnao.springbootexample.core.domain.auth.User;
import it.alnao.springbootexample.core.domain.auth.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Copre le lookup non ancora verificate in {@link UserSqlServerRepositoryImplTest}.
 */
class UserSqlServerRepositoryImplLookupTest {

    @Mock UserSqlServerJpaRepository userJpaRepository;
    @InjectMocks UserSqlServerRepositoryImpl repository;

    private UserSqlServerEntity locale;
    private UserSqlServerEntity google;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        locale = entity("u-1", "mario", "mario@test.it", AccountType.LOCAL, "ext-1");
        google = entity("u-2", "luigi", "luigi@test.it", AccountType.GOOGLE, "ext-2");
    }

    private UserSqlServerEntity entity(String id, String username, String email,
                                       AccountType accountType, String externalId) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setAccountType(accountType);
        user.setExternalId(externalId);
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        return UserSqlServerEntity.fromDomain(user);
    }

    @Test
    void existsByEmail_reflectsTheStoredRows() {
        when(userJpaRepository.findAll()).thenReturn(List.of(locale, google));
        assertTrue(repository.existsByEmail("mario@test.it"));
        assertFalse(repository.existsByEmail("assente@test.it"));
    }

    @Test
    void findByExternalIdAndAccountType_matchesBothFields() {
        when(userJpaRepository.findAll()).thenReturn(List.of(locale, google));
        Optional<User> result = repository.findByExternalIdAndAccountType("ext-2", AccountType.GOOGLE);
        assertTrue(result.isPresent());
        assertEquals("luigi", result.get().getUsername());
    }

    @Test
    void findByExternalIdAndAccountType_whenTypeDiffers_returnsEmpty() {
        when(userJpaRepository.findAll()).thenReturn(List.of(locale, google));
        assertTrue(repository.findByExternalIdAndAccountType("ext-2", AccountType.GITHUB).isEmpty());
    }

    @Test
    void findByEmailAndAccountType_matchesBothFields() {
        when(userJpaRepository.findAll()).thenReturn(List.of(locale, google));
        assertTrue(repository.findByEmailAndAccountType("mario@test.it", AccountType.LOCAL).isPresent());
        assertTrue(repository.findByEmailAndAccountType("mario@test.it", AccountType.GOOGLE).isEmpty());
    }

    @Test
    void countByAccountType_countsMatchingOnly() {
        when(userJpaRepository.findAll()).thenReturn(List.of(locale, google));
        assertEquals(1L, repository.countByAccountType(AccountType.LOCAL));
        assertEquals(0L, repository.countByAccountType(AccountType.FACEBOOK));
    }

    @Test
    void findById_whenPresent_mapsToDomain() {
        when(userJpaRepository.findById("u-1")).thenReturn(Optional.of(locale));
        Optional<User> result = repository.findById("u-1");
        assertTrue(result.isPresent());
        assertEquals("mario", result.get().getUsername());
    }

    @Test
    void findById_whenMissing_returnsEmpty() {
        when(userJpaRepository.findById("nope")).thenReturn(Optional.empty());
        assertTrue(repository.findById("nope").isEmpty());
    }

    @Test
    void findAll_mapsEveryEntity() {
        when(userJpaRepository.findAll()).thenReturn(List.of(locale, google));
        assertEquals(2, repository.findAll().size());
    }

    @Test
    void save_setsUpdatedAtOnEverySave() {
        User user = new User();
        user.setId("u-1");
        user.setUsername("mario");
        user.setEmail("mario@test.it");
        user.setAccountType(AccountType.LOCAL);
        user.setRole(UserRole.USER);
        when(userJpaRepository.save(any(UserSqlServerEntity.class))).thenReturn(locale);

        repository.save(user);

        assertNotNull(user.getUpdatedAt());
    }
}
