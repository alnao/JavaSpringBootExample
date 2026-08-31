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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserSqlServerRepositoryImplTest {

    @Mock UserSqlServerJpaRepository userJpaRepository;
    @InjectMocks UserSqlServerRepositoryImpl repository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findByUsername_whenFound_returnsDomain() {
        User user = buildUser("u-1", "user1", "u@test.com");
        when(userJpaRepository.findByUsername("user1"))
                .thenReturn(Optional.of(UserSqlServerEntity.fromDomain(user)));
        Optional<User> result = repository.findByUsername("user1");
        assertTrue(result.isPresent());
    }

    @Test
    void findByEmail_whenFound_returnsDomain() {
        User user = buildUser("u-1", "user1", "u@test.com");
        when(userJpaRepository.findByEmail("u@test.com"))
                .thenReturn(Optional.of(UserSqlServerEntity.fromDomain(user)));
        Optional<User> result = repository.findByEmail("u@test.com");
        assertTrue(result.isPresent());
    }

    @Test
    void findByEnabled_filtersCorrectly() {
        User user = buildUser("u-1", "user1", "u@test.com");
        when(userJpaRepository.findAll()).thenReturn(List.of(UserSqlServerEntity.fromDomain(user)));
        List<User> result = repository.findByEnabled(true);
        assertFalse(result.isEmpty());
    }

    @Test
    void findByAccountType_filtersCorrectly() {
        User user = buildUser("u-1", "user1", "u@test.com");
        when(userJpaRepository.findAll()).thenReturn(List.of(UserSqlServerEntity.fromDomain(user)));
        List<User> result = repository.findByAccountType(AccountType.LOCAL);
        assertFalse(result.isEmpty());
    }

    @Test
    void deleteById_delegatesToRepository() {
        repository.deleteById("u-1");
        verify(userJpaRepository).deleteById("u-1");
    }

    @Test
    void delete_delegatesToRepository() {
        User user = buildUser("u-1", "user1", "u@test.com");
        repository.delete(user);
        verify(userJpaRepository).deleteById("u-1");
    }

    @Test
    void count_delegatesToRepository() {
        when(userJpaRepository.count()).thenReturn(3L);
        assertEquals(3L, repository.count());
    }

    @Test
    void existsByUsername_filtersCorrectly() {
        User user = buildUser("u-1", "user1", "u@test.com");
        when(userJpaRepository.findAll()).thenReturn(List.of(UserSqlServerEntity.fromDomain(user)));
        assertTrue(repository.existsByUsername("user1"));
        assertFalse(repository.existsByUsername("nonexistent"));
    }

    @Test
    void save_assignsIdAndSaves() {
        User user = buildUser(null, "newuser", "new@test.com");
        User savedUser = buildUser("new-id", "newuser", "new@test.com");
        when(userJpaRepository.save(any())).thenReturn(UserSqlServerEntity.fromDomain(savedUser));
        User result = repository.save(user);
        assertNotNull(result);
    }

    private User buildUser(String id, String username, String email) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword("pass");
        u.setRole(UserRole.USER);
        u.setAccountType(AccountType.LOCAL);
        u.setEnabled(true);
        return u;
    }
}
