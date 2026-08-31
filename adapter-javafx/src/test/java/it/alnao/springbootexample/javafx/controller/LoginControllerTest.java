package it.alnao.springbootexample.javafx.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Il resto del controller manipola nodi @FXML e richiede il toolkit JavaFX;
 * authenticateUser e' invece logica pura ed e' verificabile headless.
 */
class LoginControllerTest {

    private LoginController controller;
    private Method authenticateUser;

    @BeforeEach
    void setup() throws Exception {
        controller = new LoginController(mock(ConfigurableApplicationContext.class));
        authenticateUser = LoginController.class
                .getDeclaredMethod("authenticateUser", String.class, String.class);
        authenticateUser.setAccessible(true);
    }

    private boolean authenticate(String username, String password) throws Exception {
        return (boolean) authenticateUser.invoke(controller, username, password);
    }

    @Test
    void authenticateUser_withBothCredentials_succeeds() throws Exception {
        assertTrue(authenticate("admin", "password"));
    }

    @Test
    void authenticateUser_withAnEmptyUsername_fails() throws Exception {
        assertFalse(authenticate("", "password"));
    }

    @Test
    void authenticateUser_withAnEmptyPassword_fails() throws Exception {
        assertFalse(authenticate("admin", ""));
    }

    @Test
    void authenticateUser_withBothEmpty_fails() throws Exception {
        assertFalse(authenticate("", ""));
    }
}
