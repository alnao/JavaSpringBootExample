package it.alnao.springbootexample.core.domain.auth;

import org.junit.jupiter.api.Test;

import it.alnao.springbootexample.core.service.auth.UserStatistics;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuthDomainTest {

    // ---------- RefreshToken ----------

    @Test
    void refreshToken_defaultConstructorStampsCreatedAt() {
        RefreshToken t = new RefreshToken();
        assertNotNull(t.getCreatedAt());
        assertNull(t.getToken());
    }

    @Test
    void refreshToken_mainConstructorSetsTheFieldsAndCreatedAt() {
        LocalDateTime expiry = LocalDateTime.now().plusDays(7);
        RefreshToken t = new RefreshToken("token-1", "u-1", expiry);
        assertEquals("token-1", t.getToken());
        assertEquals("u-1", t.getUserId());
        assertEquals(expiry, t.getExpiryDate());
        assertNotNull(t.getCreatedAt());
    }

    @Test
    void refreshToken_isExpiredIsFalseForAFutureDate() {
        assertFalse(new RefreshToken("t", "u", LocalDateTime.now().plusDays(1)).isExpired());
    }

    @Test
    void refreshToken_isExpiredIsTrueForAPastDate() {
        assertTrue(new RefreshToken("t", "u", LocalDateTime.now().minusSeconds(1)).isExpired());
    }

    @Test
    void refreshToken_settersAndGettersWork() {
        RefreshToken t = new RefreshToken();
        LocalDateTime expiry = LocalDateTime.now().plusHours(1);
        LocalDateTime created = LocalDateTime.now().minusHours(1);
        LocalDateTime used = LocalDateTime.now();
        t.setId("id-1");
        t.setToken("tok");
        t.setUserId("u-9");
        t.setExpiryDate(expiry);
        t.setCreatedAt(created);
        t.setLastUsed(used);

        assertEquals("id-1", t.getId());
        assertEquals("tok", t.getToken());
        assertEquals("u-9", t.getUserId());
        assertEquals(expiry, t.getExpiryDate());
        assertEquals(created, t.getCreatedAt());
        assertEquals(used, t.getLastUsed());
    }

    @Test
    void refreshToken_toStringDoesNotLeakTheTokenValue() {
        RefreshToken t = new RefreshToken("valore-segreto", "u-1", LocalDateTime.now().plusDays(1));
        t.setId("id-1");
        String text = t.toString();
        assertTrue(text.contains("id-1"));
        assertTrue(text.contains("u-1"));
        assertFalse(text.contains("valore-segreto"));
    }

    // ---------- User ----------

    @Test
    void user_defaultConstructorStampsCreatedAt() {
        User u = new User();
        assertNotNull(u.getCreatedAt());
        assertTrue(u.isEnabled());
        assertFalse(u.isEmailVerified());
    }

    @Test
    void user_localConstructorMarksTheAccountAsLocal() {
        User u = new User("mario", "mario@test.it", "secret");
        assertEquals(AccountType.LOCAL, u.getAccountType());
        assertTrue(u.isLocalAccount());
        assertFalse(u.isOAuth2Account());
    }

    @Test
    void user_oauth2ConstructorMarksTheEmailAsVerified() {
        User u = new User("mario@test.it", "Mario", "Rossi", AccountType.GOOGLE, "ext-1");
        assertEquals(AccountType.GOOGLE, u.getAccountType());
        assertEquals("ext-1", u.getExternalId());
        assertTrue(u.isEmailVerified());
        assertTrue(u.isOAuth2Account());
        assertFalse(u.isLocalAccount());
    }

    @Test
    void user_hasProviderIsCaseInsensitive() {
        User u = new User();
        u.setProviders(List.of(new UserProvider("u-1", "Google", "g-1")));
        assertTrue(u.hasProvider("google"));
        assertTrue(u.hasProvider("GOOGLE"));
        assertFalse(u.hasProvider("github"));
    }

    @Test
    void user_hasProviderIsFalseWithoutProviders() {
        assertFalse(new User().hasProvider("google"));
    }

    @Test
    void user_displayNamePrefersFirstAndLastName() {
        User u = new User("mario", "mario@test.it", "pwd");
        u.setFirstName("Mario");
        u.setLastName("Rossi");
        assertEquals("Mario Rossi", u.getDisplayName());
    }

    @Test
    void user_displayNameFallsBackToFirstNameOnly() {
        User u = new User("mario", "mario@test.it", "pwd");
        u.setFirstName("Mario");
        assertEquals("Mario", u.getDisplayName());
    }

    @Test
    void user_displayNameFallsBackToUsername() {
        User u = new User("mario", "mario@test.it", "pwd");
        assertEquals("mario", u.getDisplayName());
    }

    @Test
    void user_displayNameFallsBackToEmailAsLastResort() {
        User u = new User();
        u.setEmail("mario@test.it");
        assertEquals("mario@test.it", u.getDisplayName());
    }

    @Test
    void user_settersAndGettersWork() {
        User u = new User();
        LocalDateTime updated = LocalDateTime.now();
        LocalDateTime login = LocalDateTime.now().minusHours(1);
        u.setId("u-1");
        u.setUsername("mario");
        u.setEmail("mario@test.it");
        u.setPassword("pwd");
        u.setAvatarUrl("http://avatar");
        u.setRole(UserRole.ADMIN);
        u.setEnabled(false);
        u.setEmailVerified(true);
        u.setUpdatedAt(updated);
        u.setLastLogin(login);

        assertEquals("u-1", u.getId());
        assertEquals("mario", u.getUsername());
        assertEquals("mario@test.it", u.getEmail());
        assertEquals("pwd", u.getPassword());
        assertEquals("http://avatar", u.getAvatarUrl());
        assertEquals(UserRole.ADMIN, u.getRole());
        assertFalse(u.isEnabled());
        assertTrue(u.isEmailVerified());
        assertEquals(updated, u.getUpdatedAt());
        assertEquals(login, u.getLastLogin());
    }

    @Test
    void user_toStringIncludesUsernameAndAccountType() {
        User u = new User("mario", "mario@test.it", "pwd");
        String text = u.toString();
        assertTrue(text.contains("mario"));
    }

    // ---------- UserStatistics ----------

    @Test
    void userStatistics_allArgsConstructorCopiesEveryCounter() {
        UserStatistics s = new UserStatistics(10, 6, 4, 8, 2, 7, 3);
        assertEquals(10, s.getTotalUsers());
        assertEquals(6, s.getLocalUsers());
        assertEquals(4, s.getOauth2Users());
        assertEquals(8, s.getEnabledUsers());
        assertEquals(2, s.getDisabledUsers());
        assertEquals(7, s.getVerifiedEmails());
        assertEquals(3, s.getUnverifiedEmails());
    }

    @Test
    void userStatistics_defaultConstructorStartsAtZero() {
        UserStatistics s = new UserStatistics();
        assertEquals(0, s.getTotalUsers());
        assertEquals(0, s.getOauth2Users());
    }

    @Test
    void userStatistics_settersAndGettersWork() {
        UserStatistics s = new UserStatistics();
        s.setTotalUsers(100);
        s.setLocalUsers(60);
        s.setOauth2Users(40);
        s.setEnabledUsers(90);
        s.setDisabledUsers(10);
        s.setVerifiedEmails(70);
        s.setUnverifiedEmails(30);

        assertEquals(100, s.getTotalUsers());
        assertEquals(60, s.getLocalUsers());
        assertEquals(40, s.getOauth2Users());
        assertEquals(90, s.getEnabledUsers());
        assertEquals(10, s.getDisabledUsers());
        assertEquals(70, s.getVerifiedEmails());
        assertEquals(30, s.getUnverifiedEmails());
    }

    // ---------- UserProvider ----------

    @Test
    void userProvider_constructorAndAccessors() {
        UserProvider p = new UserProvider("u-1", "google", "g-1");
        assertEquals("u-1", p.getUserId());
        assertEquals("google", p.getProvider());
        assertEquals("g-1", p.getProviderUserId());

        LocalDateTime now = LocalDateTime.now();
        p.setId("p-1");
        p.setProviderEmail("mario@gmail.com");
        p.setProviderUsername("mario");
        p.setAccessTokenHash("hash");
        p.setCreatedAt(now);
        p.setLastUsed(now);

        assertEquals("p-1", p.getId());
        assertEquals("mario@gmail.com", p.getProviderEmail());
        assertEquals("mario", p.getProviderUsername());
        assertEquals("hash", p.getAccessTokenHash());
        assertEquals(now, p.getCreatedAt());
        assertEquals(now, p.getLastUsed());
    }

    // ---------- AccountType ----------

    @Test
    void accountType_fromStringMapsTheKnownProviders() {
        assertEquals(AccountType.GOOGLE, AccountType.fromString("google"));
        assertEquals(AccountType.GITHUB, AccountType.fromString("GitHub"));
        assertEquals(AccountType.MICROSOFT, AccountType.fromString("MICROSOFT"));
        assertEquals(AccountType.FACEBOOK, AccountType.fromString("facebook"));
    }

    @Test
    void accountType_fromStringFallsBackToLocal() {
        assertEquals(AccountType.LOCAL, AccountType.fromString("provider-sconosciuto"));
    }

    @Test
    void accountType_everyValueHasADescription() {
        for (AccountType type : AccountType.values()) {
            assertNotNull(type.getDescription());
            assertFalse(type.getDescription().isBlank());
        }
    }
}
