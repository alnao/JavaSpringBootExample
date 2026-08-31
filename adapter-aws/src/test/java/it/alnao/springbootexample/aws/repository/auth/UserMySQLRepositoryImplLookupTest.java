package it.alnao.springbootexample.aws.repository.auth;

import it.alnao.springbootexample.aws.entity.auth.UserMySQLEntity;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Copre save, findById, findByUsername, findAll e countByAccountType,
 * non verificati in {@link UserMySQLRepositoryImplMoreTest}.
 */
class UserMySQLRepositoryImplLookupTest {

    @Mock UserMySQLJpaRepository userJpaRepository;
    @InjectMocks UserMySQLRepositoryImpl repository;

    private UserMySQLEntity entity;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        User user = new User();
        user.setId("u-1");
        user.setUsername("mario");
        user.setEmail("mario@test.it");
        user.setRole(UserRole.USER);
        user.setAccountType(AccountType.LOCAL);
        user.setEnabled(true);
        entity = UserMySQLEntity.fromDomain(user);
    }

    @Test
    void save_generatesAnIdWhenMissing() {
        User user = new User();
        user.setUsername("nuovo");
        user.setEmail("nuovo@test.it");
        user.setRole(UserRole.USER);
        user.setAccountType(AccountType.LOCAL);
        when(userJpaRepository.save(any(UserMySQLEntity.class))).thenReturn(entity);

        repository.save(user);

        assertNotNull(user.getId());
    }

    @Test
    void save_keepsAnExistingIdAndStampsUpdatedAt() {
        User user = new User();
        user.setId("gia-presente");
        user.setUsername("mario");
        user.setEmail("mario@test.it");
        user.setRole(UserRole.USER);
        user.setAccountType(AccountType.LOCAL);
        when(userJpaRepository.save(any(UserMySQLEntity.class))).thenReturn(entity);

        repository.save(user);

        assertEquals("gia-presente", user.getId());
        assertNotNull(user.getUpdatedAt());
    }

    @Test
    void findById_whenPresent_mapsToDomain() {
        when(userJpaRepository.findById("u-1")).thenReturn(Optional.of(entity));
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
    void findByUsername_whenPresent_mapsToDomain() {
        when(userJpaRepository.findByUsername("mario")).thenReturn(Optional.of(entity));
        assertTrue(repository.findByUsername("mario").isPresent());
    }

    @Test
    void findAll_mapsEveryEntity() {
        when(userJpaRepository.findAll()).thenReturn(List.of(entity, entity));
        assertEquals(2, repository.findAll().size());
    }

    @Test
    void findByExternalIdAndAccountType_delegatesWithTheEnumName() {
        when(userJpaRepository.findByExternalIdAndAccountType("ext-1", AccountType.GOOGLE.name()))
                .thenReturn(Optional.of(entity));
        assertTrue(repository.findByExternalIdAndAccountType("ext-1", AccountType.GOOGLE).isPresent());
    }

    @Test
    void countByAccountType_delegatesWithTheEnumName() {
        when(userJpaRepository.countByAccountType(AccountType.LOCAL.name())).thenReturn(5L);
        assertEquals(5L, repository.countByAccountType(AccountType.LOCAL));
    }
}
