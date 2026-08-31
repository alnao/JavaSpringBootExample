package it.alnao.springbootexample.sqlite.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataInitializerPropertiesTest {

    @Test
    void defaults_areEnabledWithEmptyUserList() {
        DataInitializerProperties properties = new DataInitializerProperties();
        assertNotNull(properties.getUsers());
        assertTrue(properties.getUsers().isEnabled());
        assertTrue(properties.getUsers().getDefaultUsers().isEmpty());
    }

    @Test
    void users_settersReplaceTheWholeBlock() {
        DataInitializerProperties properties = new DataInitializerProperties();
        DataInitializerProperties.Users users = new DataInitializerProperties.Users();
        users.setEnabled(false);
        properties.setUsers(users);
        assertFalse(properties.getUsers().isEnabled());
    }

    @Test
    void userConfig_defaultsToUserRole() {
        DataInitializerProperties.UserConfig config = new DataInitializerProperties.UserConfig();
        assertEquals("USER", config.getRole());
    }

    @Test
    void userConfig_gettersAndSettersWork() {
        DataInitializerProperties.UserConfig config = new DataInitializerProperties.UserConfig();
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
        DataInitializerProperties.UserConfig config = new DataInitializerProperties.UserConfig();
        config.setUsername("mario");
        config.setRole("ADMIN");
        String text = config.toString();
        assertTrue(text.contains("mario"));
        assertTrue(text.contains("ADMIN"));
    }

    @Test
    void users_toStringIncludesEnabledFlagAndUsers() {
        DataInitializerProperties.Users users = new DataInitializerProperties.Users();
        DataInitializerProperties.UserConfig config = new DataInitializerProperties.UserConfig();
        config.setUsername("mario");
        users.setDefaultUsers(List.of(config));
        users.setEnabled(true);
        String text = users.toString();
        assertTrue(text.contains("enabled=true"));
        assertTrue(text.contains("mario"));
    }
}
