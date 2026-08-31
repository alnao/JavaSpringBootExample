package it.alnao.springbootexample.azure.entity.auth;

import it.alnao.springbootexample.core.domain.auth.AccountType;
import it.alnao.springbootexample.core.domain.auth.User;
import it.alnao.springbootexample.core.domain.auth.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserSqlServerEntityTest {

    @Test
    void fromDomain_mapsAllFields() {
        User user = buildUser("u-1", "testuser", "test@test.com", UserRole.USER);
        UserSqlServerEntity entity = UserSqlServerEntity.fromDomain(user);
        assertNotNull(entity);
        assertEquals("testuser", entity.toDomain().getUsername());
        assertEquals("test@test.com", entity.toDomain().getEmail());
        assertEquals(UserRole.USER, entity.toDomain().getRole());
    }

    @Test
    void fromDomain_withAdminRole_preservesRole() {
        User user = buildUser("u-2", "admin", "admin@test.com", UserRole.ADMIN);
        UserSqlServerEntity entity = UserSqlServerEntity.fromDomain(user);
        assertEquals(UserRole.ADMIN, entity.toDomain().getRole());
    }

    @Test
    void fromDomain_withGoogleAccountType_preservesAccountType() {
        User user = buildUser("u-3", "guser", "g@test.com", UserRole.USER);
        user.setAccountType(AccountType.GOOGLE);
        UserSqlServerEntity entity = UserSqlServerEntity.fromDomain(user);
        assertEquals(AccountType.GOOGLE, entity.toDomain().getAccountType());
    }

    @Test
    void toDomain_mapsAllFields() {
        User user = buildUser("u-1", "testuser", "test@test.com", UserRole.ADMIN);
        user.setFirstName("John");
        user.setLastName("Doe");
        UserSqlServerEntity entity = UserSqlServerEntity.fromDomain(user);
        User result = entity.toDomain();
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals(UserRole.ADMIN, result.getRole());
        assertEquals("John", result.getFirstName());
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
