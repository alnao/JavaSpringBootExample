package it.alnao.springbootexample.postgresql.entity.auth;

import it.alnao.springbootexample.core.domain.auth.AccountType;
import it.alnao.springbootexample.core.domain.auth.User;
import it.alnao.springbootexample.core.domain.auth.UserProvider;
import it.alnao.springbootexample.core.domain.auth.UserRole;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserEntityTest {

    private UserEntity base() {
        UserEntity entity = new UserEntity();
        entity.setId("u-1");
        entity.setUsername("mario");
        entity.setEmail("mario@test.it");
        entity.setRole(UserRole.USER);
        entity.setAccountType(AccountType.LOCAL);
        entity.setEnabled(true);
        return entity;
    }

    @Test
    void toDomain_withoutProviders_givesAnEmptyProviderList() {
        User user = base().toDomain();
        assertEquals("u-1", user.getId());
        assertEquals("mario", user.getUsername());
        assertTrue(user.getProviders().isEmpty());
    }

    @Test
    void toDomain_convertsEveryProvider() {
        UserEntity entity = base();
        UserProvider provider = new UserProvider("u-1", "google", "g-1");
        provider.setId("p-1");
        entity.setProviders(List.of(UserProviderEntity.fromDomain(provider)));

        User user = entity.toDomain();

        assertEquals(1, user.getProviders().size());
        assertEquals("google", user.getProviders().get(0).getProvider());
    }

    @Test
    void providersAccessorsWork() {
        UserEntity entity = base();
        assertNotNull(entity.getProviders());
        UserProvider provider = new UserProvider("u-1", "github", "h-1");
        provider.setId("p-2");
        entity.setProviders(List.of(UserProviderEntity.fromDomain(provider)));
        assertEquals(1, entity.getProviders().size());
        assertEquals("github", entity.getProviders().get(0).getProvider());
    }
}
