package it.alnao.springbootexample.core.service.auth.impl;

import it.alnao.springbootexample.core.domain.auth.AccountType;
import it.alnao.springbootexample.core.domain.auth.User;
import it.alnao.springbootexample.core.repository.auth.UserRepository;
import it.alnao.springbootexample.core.service.auth.UserStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceDefaultTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceDefault userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("mario", "mario@example.com", "password");
        user.setId("id-1");
    }

    // ---- createLocalUser ----

    @Test
    void createLocalUser_salvaERestituisce() {
        when(userRepository.save(any(User.class))).thenReturn(user);
        User result = userService.createLocalUser("mario", "mario@example.com", "pwd");
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    // ---- createOAuth2User ----

    @Test
    void createOAuth2User_salvaERestituisce() {
        when(userRepository.save(any(User.class))).thenReturn(user);
        User result = userService.createOAuth2User("mail@g.com", "Mario", "Rossi", AccountType.GOOGLE, "ext-id-1");
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    // ---- updateOAuth2User ----

    @Test
    void updateOAuth2User_aggiornaCampiESalva() {
        when(userRepository.save(any(User.class))).thenReturn(user);
        User result = userService.updateOAuth2User(user, "Luigi", "Verdi", "http://avatar");
        assertEquals("Luigi", user.getFirstName());
        assertEquals("Verdi", user.getLastName());
        assertEquals("http://avatar", user.getAvatarUrl());
    }

    // ---- findById ----

    @Test
    void findById_trovato_ritornaPresent() {
        when(userRepository.findById("id-1")).thenReturn(Optional.of(user));
        assertTrue(userService.findById("id-1").isPresent());
    }

    @Test
    void findById_nonTrovato_ritornaEmpty() {
        when(userRepository.findById("id-x")).thenReturn(Optional.empty());
        assertTrue(userService.findById("id-x").isEmpty());
    }

    // ---- findByUsername ----

    @Test
    void findByUsername_trovato() {
        when(userRepository.findByUsername("mario")).thenReturn(Optional.of(user));
        assertTrue(userService.findByUsername("mario").isPresent());
    }

    // ---- findByEmail ----

    @Test
    void findByEmail_trovato() {
        when(userRepository.findByEmail("mario@example.com")).thenReturn(Optional.of(user));
        assertTrue(userService.findByEmail("mario@example.com").isPresent());
    }

    // ---- findByEmailAndProvider ----

    @Test
    void findByEmailAndProvider_delega_findByEmail() {
        when(userRepository.findByEmail("mario@example.com")).thenReturn(Optional.of(user));
        assertTrue(userService.findByEmailAndProvider("mario@example.com", "google").isPresent());
        verify(userRepository).findByEmail("mario@example.com");
    }

    // ---- findByExternalIdAndAccountType ----

    @Test
    void findByExternalIdAndAccountType_delegaARepository() {
        when(userRepository.findByExternalIdAndAccountType("ext", AccountType.GOOGLE))
                .thenReturn(Optional.of(user));
        assertTrue(userService.findByExternalIdAndAccountType("ext", AccountType.GOOGLE).isPresent());
    }

    // ---- existsByEmail / existsByUsername ----

    @Test
    void existsByEmail_ritornaValore() {
        when(userRepository.existsByEmail("mario@example.com")).thenReturn(true);
        assertTrue(userService.existsByEmail("mario@example.com"));
    }

    @Test
    void existsByUsername_ritornaValore() {
        when(userRepository.existsByUsername("mario")).thenReturn(false);
        assertFalse(userService.existsByUsername("mario"));
    }

    // ---- updateLastLogin ----

    @Test
    void updateLastLogin_utenteEsiste_aggiornaSalva() {
        when(userRepository.findById("id-1")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);
        userService.updateLastLogin("id-1");
        verify(userRepository).save(user);
        assertNotNull(user.getLastLogin());
    }

    @Test
    void updateLastLogin_utenteNonEsiste_nonSalva() {
        when(userRepository.findById("id-x")).thenReturn(Optional.empty());
        userService.updateLastLogin("id-x");
        verify(userRepository, never()).save(any());
    }

    // ---- changePassword ----

    @Test
    void changePassword_utenteEsiste_aggiorna() {
        when(userRepository.findById("id-1")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);
        userService.changePassword("id-1", "newpwd");
        assertEquals("newpwd", user.getPassword());
    }

    // ---- setUserEnabled ----

    @Test
    void setUserEnabled_disabilita() {
        when(userRepository.findById("id-1")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);
        userService.setUserEnabled("id-1", false);
        assertFalse(user.isEnabled());
    }

    // ---- verifyEmail ----

    @Test
    void verifyEmail_impostaEmailVerified() {
        when(userRepository.findById("id-1")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);
        userService.verifyEmail("id-1");
        assertTrue(user.isEmailVerified());
    }

    // ---- findAll / findByAccountType ----

    @Test
    void findAllUsers_delegaARepository() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));
        assertEquals(1, userService.findAllUsers().size());
    }

    @Test
    void findUsersByAccountType_delegaARepository() {
        when(userRepository.findByAccountType(AccountType.LOCAL)).thenReturn(Collections.singletonList(user));
        assertEquals(1, userService.findUsersByAccountType(AccountType.LOCAL).size());
    }

    // ---- getUserStatistics ----

    @Test
    void getUserStatistics_ritornaStatistiche() {
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.countByAccountType(AccountType.LOCAL)).thenReturn(7L);
        when(userRepository.findByEnabled(true)).thenReturn(Arrays.asList(user, user));

        UserStatistics stats = userService.getUserStatistics();
        assertEquals(10L, stats.getTotalUsers());
        assertEquals(7L, stats.getLocalUsers());
        assertEquals(2L, stats.getEnabledUsers());
    }

    // ---- deleteUser ----

    @Test
    void deleteUser_delegaARepository() {
        userService.deleteUser("id-1");
        verify(userRepository).deleteById("id-1");
    }

    // ---- linkOAuth2Provider ----

    @Test
    void linkOAuth2Provider_aggiornaCampi() {
        when(userRepository.save(any())).thenReturn(user);
        userService.linkOAuth2Provider(user, "google", "ext-id", "altro@g.com");
        assertEquals("ext-id", user.getExternalId());
        assertEquals("altro@g.com", user.getEmail());
    }

    // ---- unlinkOAuth2Provider ----

    @Test
    void unlinkOAuth2Provider_pulisceExternalId() {
        user.setExternalId("ext-id");
        when(userRepository.save(any())).thenReturn(user);
        userService.unlinkOAuth2Provider(user, "google");
        assertNull(user.getExternalId());
    }
}
