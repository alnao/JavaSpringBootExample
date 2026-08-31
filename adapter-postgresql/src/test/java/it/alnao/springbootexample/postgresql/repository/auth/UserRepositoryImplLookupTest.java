package it.alnao.springbootexample.postgresql.repository.auth;

import it.alnao.springbootexample.core.domain.auth.AccountType;
import it.alnao.springbootexample.core.domain.auth.User;
import it.alnao.springbootexample.postgresql.entity.auth.UserEntity;
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
 * Copre le lookup non verificate in {@link UserRepositoryImplTest}.
 */
class UserRepositoryImplLookupTest {

    @Mock UserJpaRepository jpaRepository;
    @InjectMocks UserRepositoryImpl repository;

    private UserEntity entity;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        entity = new UserEntity();
        entity.setId("u-1");
        entity.setUsername("mario");
        entity.setEmail("mario@test.it");
        entity.setAccountType(AccountType.LOCAL);
        entity.setExternalId("ext-1");
        entity.setEnabled(true);
    }

    @Test
    void findByEmailAndAccountType_delegates() {
        when(jpaRepository.findByEmailAndAccountType("mario@test.it", AccountType.LOCAL))
                .thenReturn(Optional.of(entity));
        assertTrue(repository.findByEmailAndAccountType("mario@test.it", AccountType.LOCAL).isPresent());
    }

    @Test
    void findByEmailAndAccountType_whenMissingReturnsEmpty() {
        when(jpaRepository.findByEmailAndAccountType("assente@test.it", AccountType.GOOGLE))
                .thenReturn(Optional.empty());
        assertTrue(repository.findByEmailAndAccountType("assente@test.it", AccountType.GOOGLE).isEmpty());
    }

    @Test
    void findByExternalIdAndAccountType_delegates() {
        when(jpaRepository.findByExternalIdAndAccountType("ext-1", AccountType.LOCAL))
                .thenReturn(Optional.of(entity));
        Optional<User> result = repository.findByExternalIdAndAccountType("ext-1", AccountType.LOCAL);
        assertTrue(result.isPresent());
        assertEquals("mario", result.get().getUsername());
    }

    @Test
    void existsByUsername_delegates() {
        when(jpaRepository.existsByUsername("mario")).thenReturn(true);
        when(jpaRepository.existsByUsername("fantasma")).thenReturn(false);
        assertTrue(repository.existsByUsername("mario"));
        assertFalse(repository.existsByUsername("fantasma"));
    }

    @Test
    void findByAccountType_delegatesAndMaps() {
        when(jpaRepository.findByAccountType(AccountType.LOCAL)).thenReturn(List.of(entity));
        assertEquals(1, repository.findByAccountType(AccountType.LOCAL).size());
    }

    @Test
    void findByEnabled_delegatesAndMaps() {
        when(jpaRepository.findByEnabled(true)).thenReturn(List.of(entity));
        assertEquals(1, repository.findByEnabled(true).size());
    }

    @Test
    void delete_usesTheUserId() {
        User user = new User();
        user.setId("u-1");
        repository.delete(user);
        verify(jpaRepository).deleteById("u-1");
    }

    @Test
    void countByAccountType_delegates() {
        when(jpaRepository.countByAccountType(AccountType.GOOGLE)).thenReturn(3L);
        assertEquals(3L, repository.countByAccountType(AccountType.GOOGLE));
    }
}
