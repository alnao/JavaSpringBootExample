package it.alnao.springbootexample.core.domain.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountTypeTest {

    @Test
    void valoriEnum_esistono() {
        assertNotNull(AccountType.LOCAL);
        assertNotNull(AccountType.GOOGLE);
        assertNotNull(AccountType.GITHUB);
        assertNotNull(AccountType.MICROSOFT);
        assertNotNull(AccountType.FACEBOOK);
    }

    @Test
    void getDescription_nonnull() {
        for (AccountType type : AccountType.values()) {
            assertNotNull(type.getDescription());
            assertFalse(type.getDescription().isEmpty());
        }
    }

    @Test
    void fromString_google() {
        assertEquals(AccountType.GOOGLE, AccountType.fromString("google"));
        assertEquals(AccountType.GOOGLE, AccountType.fromString("GOOGLE"));
    }

    @Test
    void fromString_github() {
        assertEquals(AccountType.GITHUB, AccountType.fromString("github"));
    }

    @Test
    void fromString_microsoft() {
        assertEquals(AccountType.MICROSOFT, AccountType.fromString("microsoft"));
    }

    @Test
    void fromString_facebook() {
        assertEquals(AccountType.FACEBOOK, AccountType.fromString("facebook"));
    }

    @Test
    void fromString_sconosciuto_ritornLocal() {
        assertEquals(AccountType.LOCAL, AccountType.fromString("sconosciuto"));
        assertEquals(AccountType.LOCAL, AccountType.fromString(""));
    }
}
