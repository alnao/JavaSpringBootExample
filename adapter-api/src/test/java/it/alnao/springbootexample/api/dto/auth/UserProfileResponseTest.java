package it.alnao.springbootexample.api.dto.auth;

import it.alnao.springbootexample.core.domain.auth.AccountType;
import it.alnao.springbootexample.core.domain.auth.User;
import it.alnao.springbootexample.core.domain.auth.UserRole;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserProfileResponseTest {

    @Test
    void noArgConstructor_works() {
        UserProfileResponse r = new UserProfileResponse();
        assertNotNull(r);
    }

    @Test
    void from_mapsUserToResponse() {
        User user = new User();
        user.setId("id-1");
        user.setUsername("user1");
        user.setEmail("user1@test.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setAvatarUrl("http://avatar");
        user.setAccountType(AccountType.LOCAL);
        user.setRole(UserRole.USER);
        user.setEnabled(true);

        UserProfileResponse r = UserProfileResponse.from(user);

        assertNotNull(r);
        assertEquals("id-1", r.getId());
        assertEquals("user1", r.getUsername());
        assertEquals("user1@test.com", r.getEmail());
        assertEquals("John", r.getFirstName());
        assertEquals("Doe", r.getLastName());
        assertEquals("http://avatar", r.getAvatarUrl());
        assertEquals(AccountType.LOCAL, r.getAccountType());
        assertTrue(r.isEnabled());
    }

    @Test
    void gettersAndSetters_workCorrectly() {
        UserProfileResponse r = new UserProfileResponse();
        r.setId("id-1");
        r.setUsername("user1");
        r.setEmail("e@test.com");
        r.setFirstName("First");
        r.setLastName("Last");
        r.setAvatarUrl("http://url");
        r.setAccountType(AccountType.GOOGLE);
        r.setLinkedProviders(List.of("google", "github"));
        r.setEnabled(false);

        assertEquals("id-1", r.getId());
        assertEquals("user1", r.getUsername());
        assertEquals("e@test.com", r.getEmail());
        assertEquals("First", r.getFirstName());
        assertEquals("Last", r.getLastName());
        assertEquals("http://url", r.getAvatarUrl());
        assertEquals(AccountType.GOOGLE, r.getAccountType());
        assertEquals(List.of("google", "github"), r.getLinkedProviders());
        assertFalse(r.isEnabled());
    }
}
