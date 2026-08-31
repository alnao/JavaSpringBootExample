package it.alnao.springbootexample.sqlite.config;

import it.alnao.springbootexample.core.domain.auth.AccountType;
import it.alnao.springbootexample.core.domain.auth.User;
import it.alnao.springbootexample.core.domain.auth.UserRole;
import it.alnao.springbootexample.core.repository.auth.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DataInitializerTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock ApplicationArguments applicationArguments;

    private DataInitializerProperties properties;
    private DataInitializer initializer;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        properties = new DataInitializerProperties();
        initializer = new DataInitializer(userRepository, passwordEncoder, properties);
    }

    private DataInitializerProperties.UserConfig userConfig(String username, String role) {
        DataInitializerProperties.UserConfig c = new DataInitializerProperties.UserConfig();
        c.setUsername(username);
        c.setEmail(username + "@test.it");
        c.setPassword("secret");
        c.setRole(role);
        return c;
    }

    @Test
    void run_whenDisabled_createsNoUser() throws Exception {
        properties.getUsers().setEnabled(false);
        properties.getUsers().setDefaultUsers(List.of(userConfig("admin", "ADMIN")));

        initializer.run(applicationArguments);

        verify(userRepository, never()).save(any());
        verify(userRepository, never()).existsByUsername(anyString());
    }

    @Test
    void run_whenEnabled_createsMissingUsers() throws Exception {
        properties.getUsers().setEnabled(true);
        properties.getUsers().setDefaultUsers(List.of(userConfig("admin", "ADMIN")));
        when(userRepository.existsByUsername("admin")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");

        initializer.run(applicationArguments);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User created = captor.getValue();
        assertEquals("admin", created.getUsername());
        assertEquals("admin@test.it", created.getEmail());
        assertEquals("encoded-secret", created.getPassword());
        assertEquals(UserRole.ADMIN, created.getRole());
        assertEquals(AccountType.LOCAL, created.getAccountType());
        assertTrue(created.isEnabled());
        assertTrue(created.isEmailVerified());
        assertNotNull(created.getId());
        assertNotNull(created.getCreatedAt());
        assertNotNull(created.getUpdatedAt());
    }

    @Test
    void run_whenUserAlreadyExists_skipsCreation() throws Exception {
        properties.getUsers().setEnabled(true);
        properties.getUsers().setDefaultUsers(List.of(userConfig("admin", "ADMIN")));
        when(userRepository.existsByUsername("admin")).thenReturn(true);

        initializer.run(applicationArguments);

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void run_createsOnlyTheMissingOnesOfSeveral() throws Exception {
        properties.getUsers().setEnabled(true);
        properties.getUsers().setDefaultUsers(List.of(
                userConfig("admin", "ADMIN"),
                userConfig("mario", "USER"),
                userConfig("luigi", "USER")));
        when(userRepository.existsByUsername("admin")).thenReturn(true);
        when(userRepository.existsByUsername("mario")).thenReturn(false);
        when(userRepository.existsByUsername("luigi")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");

        initializer.run(applicationArguments);

        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    void run_withEmptyUserList_savesNothing() throws Exception {
        properties.getUsers().setEnabled(true);
        properties.getUsers().setDefaultUsers(List.of());

        initializer.run(applicationArguments);

        verify(userRepository, never()).save(any());
    }
}
