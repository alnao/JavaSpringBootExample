package it.alnao.springbootexample.postgresql.repository.auth;

import it.alnao.springbootexample.postgresql.entity.auth.UserEntity;
import it.alnao.springbootexample.core.domain.auth.User;
import it.alnao.springbootexample.core.domain.auth.AccountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryImplTest {

    @Mock
    private UserJpaRepository jpaRepository;

    @InjectMocks
    private UserRepositoryImpl repository;

    private UserEntity entity;
    private User user;

    @BeforeEach
    void setUp() {
        entity = new UserEntity();
        entity.setId("user-1");
        entity.setUsername("testuser");
        entity.setEmail("test@example.com");
        
        user = new User();
        user.setId("user-1");
        user.setUsername("testuser");
        user.setEmail("test@example.com");
    }

    @Test
    void testSave() {
        when(jpaRepository.save(any(UserEntity.class))).thenReturn(entity);
        
        User result = repository.save(user);
        
        assertNotNull(result);
        assertEquals("user-1", result.getId());
        assertEquals("testuser", result.getUsername());
        verify(jpaRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testFindById() {
        when(jpaRepository.findByIdWithProviders("user-1")).thenReturn(Optional.of(entity));
        
        Optional<User> result = repository.findById("user-1");
        
        assertTrue(result.isPresent());
        assertEquals("user-1", result.get().getId());
        verify(jpaRepository, times(1)).findByIdWithProviders("user-1");
    }

    @Test
    void testFindByUsername() {
        when(jpaRepository.findByUsername("testuser")).thenReturn(Optional.of(entity));
        
        Optional<User> result = repository.findByUsername("testuser");
        
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(jpaRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void testFindByEmail() {
        when(jpaRepository.findByEmail("test@example.com")).thenReturn(Optional.of(entity));
        
        Optional<User> result = repository.findByEmail("test@example.com");
        
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
        verify(jpaRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void testExistsByEmail() {
        when(jpaRepository.existsByEmail("test@example.com")).thenReturn(true);
        
        boolean result = repository.existsByEmail("test@example.com");
        
        assertTrue(result);
        verify(jpaRepository, times(1)).existsByEmail("test@example.com");
    }

    @Test
    void testFindAll() {
        when(jpaRepository.findAll()).thenReturn(Arrays.asList(entity));
        
        List<User> result = repository.findAll();
        
        assertEquals(1, result.size());
        verify(jpaRepository, times(1)).findAll();
    }

    @Test
    void testDeleteById() {
        doNothing().when(jpaRepository).deleteById("user-1");
        
        repository.deleteById("user-1");
        
        verify(jpaRepository, times(1)).deleteById("user-1");
    }

    @Test
    void testCount() {
        when(jpaRepository.count()).thenReturn(5L);
        
        long result = repository.count();
        
        assertEquals(5L, result);
        verify(jpaRepository, times(1)).count();
    }
}
