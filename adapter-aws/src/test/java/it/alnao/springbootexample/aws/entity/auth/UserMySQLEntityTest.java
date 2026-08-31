package it.alnao.springbootexample.aws.entity.auth;

import it.alnao.springbootexample.core.domain.auth.AccountType;
import it.alnao.springbootexample.core.domain.auth.User;
import it.alnao.springbootexample.core.domain.auth.UserRole;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserMySQLEntityTest {

    @Test
    void noArgConstructor_setsCreatedAt() {
        UserMySQLEntity e = new UserMySQLEntity();
        assertNotNull(e.getCreatedAt());
    }

    @Test
    void gettersAndSetters_workCorrectly() {
        UserMySQLEntity e = new UserMySQLEntity();
        LocalDateTime now = LocalDateTime.now();
        e.setId("id-1");
        e.setUsername("user1");
        e.setEmail("user1@test.com");
        e.setPassword("pwd");
        e.setFirstName("First");
        e.setLastName("Last");
        e.setAvatarUrl("http://avatar.url");
        e.setRole(UserRole.ADMIN);
        e.setAccountType(AccountType.LOCAL);
        e.setExternalId("ext-123");
        e.setEnabled(false);
        e.setEmailVerified(true);
        e.setCreatedAt(now);
        e.setUpdatedAt(now);
        e.setLastLogin(now);

        assertEquals("id-1", e.getId());
        assertEquals("user1", e.getUsername());
        assertEquals("user1@test.com", e.getEmail());
        assertEquals("pwd", e.getPassword());
        assertEquals("First", e.getFirstName());
        assertEquals("Last", e.getLastName());
        assertEquals("http://avatar.url", e.getAvatarUrl());
        assertEquals(UserRole.ADMIN, e.getRole());
        assertEquals(AccountType.LOCAL, e.getAccountType());
        assertEquals("ext-123", e.getExternalId());
        assertFalse(e.getEnabled());
        assertTrue(e.getEmailVerified());
        assertEquals(now, e.getCreatedAt());
        assertEquals(now, e.getUpdatedAt());
        assertEquals(now, e.getLastLogin());
    }

    @Test
    void fromDomain_mapsAllFields() {
        User user = buildUser("u-1", "testuser", "test@test.com", UserRole.USER);
        UserMySQLEntity entity = UserMySQLEntity.fromDomain(user);
        assertNotNull(entity);
        assertEquals("testuser", entity.getUsername());
        assertEquals("test@test.com", entity.getEmail());
        assertEquals(UserRole.USER, entity.getRole());
        assertEquals(AccountType.LOCAL, entity.getAccountType());
    }

    @Test
    void toDomain_mapsAllFields() {
        UserMySQLEntity e = new UserMySQLEntity();
        e.setId("id-1");
        e.setUsername("testuser");
        e.setEmail("test@test.com");
        e.setPassword("pass");
        e.setRole(UserRole.USER);
        e.setAccountType(AccountType.LOCAL);
        e.setEnabled(true);
        e.setEmailVerified(false);

        User user = e.toDomain();

        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
        assertEquals("test@test.com", user.getEmail());
        assertEquals(UserRole.USER, user.getRole());
        assertEquals(AccountType.LOCAL, user.getAccountType());
    }

    @Test
    void fromDomain_withNullFields_doesNotThrow() {
        User user = new User();
        user.setId("u-2");
        user.setEmail("e@test.com");
        user.setRole(UserRole.USER);
        user.setAccountType(AccountType.LOCAL);
        assertDoesNotThrow(() -> UserMySQLEntity.fromDomain(user));
    }

    private User buildUser(String id, String username, String email, UserRole role) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword("pass");
        u.setRole(role);
        u.setAccountType(AccountType.LOCAL);
        u.setEnabled(true);
        return u;
    }
}
