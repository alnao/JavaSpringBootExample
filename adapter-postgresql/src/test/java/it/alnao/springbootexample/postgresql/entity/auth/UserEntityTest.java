package it.alnao.springbootexample.postgresql.entity.auth;

import it.alnao.springbootexample.core.domain.auth.User;
import it.alnao.springbootexample.core.domain.auth.UserRole;
import it.alnao.springbootexample.core.domain.auth.AccountType;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class UserEntityTest {

    @Test
    void testDefaultConstructor() {
        UserEntity entity = new UserEntity();
        
        assertNotNull(entity);
        assertNotNull(entity.getCreatedAt());
        assertEquals(UserRole.USER, entity.getRole());
        assertEquals(AccountType.LOCAL, entity.getAccountType());
        assertTrue(entity.isEnabled());
        assertFalse(entity.isEmailVerified());
    }

    @Test
    void testGettersAndSetters() {
        UserEntity entity = new UserEntity();
        LocalDateTime now = LocalDateTime.now();
        
        entity.setId("user-1");
        entity.setUsername("testuser");
        entity.setEmail("test@example.com");
        entity.setPassword("hashedpass");
        entity.setFirstName("John");
        entity.setLastName("Doe");
        entity.setAvatarUrl("http://avatar.url");
        entity.setRole(UserRole.ADMIN);
        entity.setAccountType(AccountType.GOOGLE);
        entity.setExternalId("ext-123");
        entity.setEnabled(false);
        entity.setEmailVerified(true);
        entity.setCreatedAt(now);
        entity.setLastLogin(now);
        
        assertEquals("user-1", entity.getId());
        assertEquals("testuser", entity.getUsername());
        assertEquals("test@example.com", entity.getEmail());
        assertEquals("hashedpass", entity.getPassword());
        assertEquals("John", entity.getFirstName());
        assertEquals("Doe", entity.getLastName());
        assertEquals("http://avatar.url", entity.getAvatarUrl());
        assertEquals(UserRole.ADMIN, entity.getRole());
        assertEquals(AccountType.GOOGLE, entity.getAccountType());
        assertEquals("ext-123", entity.getExternalId());
        assertFalse(entity.isEnabled());
        assertTrue(entity.isEmailVerified());
        assertEquals(now, entity.getCreatedAt());
        assertEquals(now, entity.getLastLogin());
    }

    @Test
    void testFromDomain() {
        User user = new User();
        user.setId("user-1");
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("pass");
        user.setRole(UserRole.ADMIN);
        
        UserEntity entity = UserEntity.fromDomain(user);
        
        assertEquals("user-1", entity.getId());
        assertEquals("testuser", entity.getUsername());
        assertEquals("test@example.com", entity.getEmail());
        assertEquals("pass", entity.getPassword());
        assertEquals(UserRole.ADMIN, entity.getRole());
    }

    @Test
    void testToDomain() {
        UserEntity entity = new UserEntity();
        entity.setId("user-1");
        entity.setUsername("testuser");
        entity.setEmail("test@example.com");
        entity.setPassword("pass");
        entity.setRole(UserRole.USER);
        
        User user = entity.toDomain();
        
        assertEquals("user-1", user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("pass", user.getPassword());
        assertEquals(UserRole.USER, user.getRole());
    }
}
