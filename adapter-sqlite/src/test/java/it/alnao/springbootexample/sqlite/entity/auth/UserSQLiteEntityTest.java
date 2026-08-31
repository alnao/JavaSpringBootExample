package it.alnao.springbootexample.sqlite.entity.auth;

import it.alnao.springbootexample.core.domain.auth.AccountType;
import it.alnao.springbootexample.core.domain.auth.User;
import it.alnao.springbootexample.core.domain.auth.UserRole;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserSQLiteEntityTest {

    @Test
    void noArgConstructor_works() {
        UserSQLiteEntity e = new UserSQLiteEntity();
        assertNotNull(e);
    }

    @Test
    void domainConstructor_mapsAllFields() {
        User user = buildUser("u-1", "testuser", "test@test.com");
        UserSQLiteEntity e = new UserSQLiteEntity(user);
        assertEquals("testuser", e.getUsername());
        assertEquals("test@test.com", e.getEmail());
    }

    @Test
    void gettersAndSetters_workCorrectly() {
        UserSQLiteEntity e = new UserSQLiteEntity();
        LocalDateTime now = LocalDateTime.now();
        e.setId("id-1");
        e.setUsername("user1");
        e.setEmail("user1@test.com");
        e.setPassword("pwd");
        e.setFirstName("First");
        e.setLastName("Last");
        e.setAvatarUrl("http://avatar.url");
        e.setUserRole(UserRole.ADMIN);
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
        assertEquals(UserRole.ADMIN, e.getUserRole());
        assertEquals(AccountType.LOCAL, e.getAccountType());
        assertEquals("ext-123", e.getExternalId());
        assertFalse(e.isEnabled());
        assertTrue(e.isEmailVerified());
        assertEquals(now, e.getCreatedAt());
        assertEquals(now, e.getUpdatedAt());
        assertEquals(now, e.getLastLogin());
    }

    @Test
    void fromDomain_mapsAllFields() {
        User user = buildUser("u-1", "testuser", "test@test.com");
        UserSQLiteEntity entity = UserSQLiteEntity.fromDomain(user);
        assertNotNull(entity);
        assertEquals("testuser", entity.getUsername());
        assertEquals("test@test.com", entity.getEmail());
    }

    @Test
    void toDomain_mapsAllFields() {
        User user = buildUser("u-1", "testuser", "test@test.com");
        UserSQLiteEntity e = new UserSQLiteEntity(user);
        e.setId("id-1");

        User result = e.toDomain();

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@test.com", result.getEmail());
        assertEquals(UserRole.USER, result.getRole());
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
