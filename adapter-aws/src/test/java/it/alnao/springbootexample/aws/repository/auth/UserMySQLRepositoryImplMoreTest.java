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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserMySQLRepositoryImplMoreTest {

    @Mock UserMySQLJpaRepository userJpaRepository;
    @InjectMocks UserMySQLRepositoryImpl repository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findByEmail_whenFound_returnsDomain() {
        UserMySQLEntity entity = buildEntity("u-1", "user1", "u@test.com");
        when(userJpaRepository.findByEmail("u@test.com")).thenReturn(Optional.of(entity));
        Optional<User> result = repository.findByEmail("u@test.com");
        assertTrue(result.isPresent());
        assertEquals("user1", result.get().getUsername());
    }

    @Test
    void findByEmail_whenNotFound_returnsEmpty() {
        when(userJpaRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertTrue(repository.findByEmail("none@test.com").isEmpty());
    }

    @Test
    void existsByUsername_delegatesToRepository() {
        when(userJpaRepository.existsByUsername("user1")).thenReturn(true);
        assertTrue(repository.existsByUsername("user1"));
    }

    @Test
    void existsByEmail_delegatesToRepository() {
        when(userJpaRepository.existsByEmail("u@test.com")).thenReturn(true);
        assertTrue(repository.existsByEmail("u@test.com"));
    }

    @Test
    void findByEmailAndAccountType_whenFound_returnsDomain() {
        UserMySQLEntity entity = buildEntity("u-1", "user1", "u@test.com");
        when(userJpaRepository.findByEmailAndAccountType("u@test.com", AccountType.LOCAL.name()))
                .thenReturn(Optional.of(entity));
        Optional<User> result = repository.findByEmailAndAccountType("u@test.com", AccountType.LOCAL);
        assertTrue(result.isPresent());
    }

    @Test
    void delete_delegatesToRepository() {
        User user = new User();
        user.setId("u-1");
        repository.delete(user);
        verify(userJpaRepository).deleteById("u-1");
    }

    @Test
    void deleteById_delegatesToRepository() {
        repository.deleteById("u-1");
        verify(userJpaRepository).deleteById("u-1");
    }

    @Test
    void count_delegatesToRepository() {
        when(userJpaRepository.count()).thenReturn(5L);
        assertEquals(5L, repository.count());
    }

    @Test
    void findByEnabled_filtersByEnabled() {
        UserMySQLEntity entity = buildEntity("u-1", "user1", "u@test.com");
        when(userJpaRepository.findByEnabled(true)).thenReturn(List.of(entity));
        List<User> result = repository.findByEnabled(true);
        assertFalse(result.isEmpty());
    }

    @Test
    void findByAccountType_filtersCorrectly() {
        UserMySQLEntity entity = buildEntity("u-1", "user1", "u@test.com");
        when(userJpaRepository.findByAccountType(AccountType.LOCAL.name())).thenReturn(List.of(entity));
        List<User> result = repository.findByAccountType(AccountType.LOCAL);
        assertFalse(result.isEmpty());
    }

    private UserMySQLEntity buildEntity(String id, String username, String email) {
        UserMySQLEntity e = new UserMySQLEntity();
        e.setId(id);
        e.setUsername(username);
        e.setEmail(email);
        e.setRole(UserRole.USER);
        e.setAccountType(AccountType.LOCAL);
        e.setEnabled(true);
        return e;
    }
}
