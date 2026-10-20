package it.alnao.springbootexample.core.config.init;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UtentiInizialiPropertiesTest {

    @Test
    void defaults_areEnabledWithEmptyUserList() {
        UtentiInizialiProperties properties = new UtentiInizialiProperties();
        assertNotNull(properties.getUsers());
        assertTrue(properties.getUsers().isEnabled());
        assertTrue(properties.getUsers().getDefaultUsers().isEmpty());
    }

    @Test
    void users_settersReplaceTheWholeBlock() {
        UtentiInizialiProperties properties = new UtentiInizialiProperties();
        UtentiInizialiProperties.Users users = new UtentiInizialiProperties.Users();
        users.setEnabled(false);
        properties.setUsers(users);
        assertFalse(properties.getUsers().isEnabled());
    }

    @Test
    void userConfig_defaultsToUserRole() {
        UtentiInizialiProperties.UserConfig config = new UtentiInizialiProperties.UserConfig();
        assertEquals("USER", config.getRole());
    }

    @Test
    void userConfig_gettersAndSettersWork() {
        UtentiInizialiProperties.UserConfig config = new UtentiInizialiProperties.UserConfig();
        config.setUsername("mario");
        config.setEmail("mario@test.it");
        config.setPassword("pwd");
        config.setRole("ADMIN");
        assertEquals("mario", config.getUsername());
        assertEquals("mario@test.it", config.getEmail());
        assertEquals("pwd", config.getPassword());
        assertEquals("ADMIN", config.getRole());
    }

    @Test
    void userConfig_toStringIncludesUsernameAndRole() {
        UtentiInizialiProperties.UserConfig config = new UtentiInizialiProperties.UserConfig();
        config.setUsername("mario");
        config.setRole("ADMIN");
        String text = config.toString();
        assertTrue(text.contains("mario"));
        assertTrue(text.contains("ADMIN"));
    }

    @Test
    void userConfig_toStringMascheraLaPassword() {
        UtentiInizialiProperties.UserConfig config = new UtentiInizialiProperties.UserConfig();
        config.setUsername("mario");
        config.setPassword("Segreta-123");
        String text = config.toString();
        assertFalse(text.contains("Segreta-123"));
        assertTrue(text.contains("****"));
    }

    @Test
    void users_toStringNonContieneLePassword() {
        UtentiInizialiProperties.Users users = new UtentiInizialiProperties.Users();
        UtentiInizialiProperties.UserConfig config = new UtentiInizialiProperties.UserConfig();
        config.setUsername("mario");
        config.setPassword("Segreta-123");
        users.setDefaultUsers(List.of(config));
        String text = users.toString();
        assertFalse(text.contains("Segreta-123"));
        assertTrue(text.contains("****"));
    }

    @Test
    void users_toStringIncludesEnabledFlagAndUsers() {
        UtentiInizialiProperties.Users users = new UtentiInizialiProperties.Users();
        UtentiInizialiProperties.UserConfig config = new UtentiInizialiProperties.UserConfig();
        config.setUsername("mario");
        users.setDefaultUsers(List.of(config));
        users.setEnabled(true);
        String text = users.toString();
        assertTrue(text.contains("enabled=true"));
        assertTrue(text.contains("mario"));
    }
}
