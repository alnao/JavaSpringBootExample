package it.alnao.springbootexample.core.security;

import it.alnao.springbootexample.core.domain.auth.AccountType;
import it.alnao.springbootexample.core.domain.auth.User;
import it.alnao.springbootexample.core.domain.auth.UserRole;
import it.alnao.springbootexample.core.service.auth.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User localUser;

    @BeforeEach
    void setUp() {
        localUser = new User("mario", "mario@example.com", "hashedpwd");
        localUser.setId("id-1");
        localUser.setRole(UserRole.USER);
        localUser.setAccountType(AccountType.LOCAL);
        localUser.setEnabled(true);
    }

    @Test
    void loadUserByUsername_utenteLocaleEsistente_ritornaUserDetails() {
        when(userService.findByUsername("mario")).thenReturn(Optional.of(localUser));
        UserDetails details = customUserDetailsService.loadUserByUsername("mario");
        assertNotNull(details);
        assertEquals("mario", details.getUsername());
        assertEquals("hashedpwd", details.getPassword());
        assertFalse(details.getAuthorities().isEmpty());
    }

    @Test
    void loadUserByUsername_utenteNonTrovato_lanciaUsernameNotFoundException() {
        when(userService.findByUsername("ignoto")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () ->
                customUserDetailsService.loadUserByUsername("ignoto"));
    }

    @Test
    void loadUserByUsername_utenteOAuth2_lanciaUsernameNotFoundException() {
        User oauthUser = new User("oauth@g.com", "Mario", "Rossi", AccountType.GOOGLE, "ext-id");
        oauthUser.setUsername("oauthuser");
        when(userService.findByUsername("oauthuser")).thenReturn(Optional.of(oauthUser));
        assertThrows(UsernameNotFoundException.class, () ->
                customUserDetailsService.loadUserByUsername("oauthuser"));
    }

    @Test
    void loadUserByUsername_utenteDisabilitato_accountBloccato() {
        localUser.setEnabled(false);
        when(userService.findByUsername("mario")).thenReturn(Optional.of(localUser));
        UserDetails details = customUserDetailsService.loadUserByUsername("mario");
        assertFalse(details.isAccountNonLocked());
    }

    @Test
    void loadUserByUsername_utenteAttivo_accountNonBloccato() {
        when(userService.findByUsername("mario")).thenReturn(Optional.of(localUser));
        UserDetails details = customUserDetailsService.loadUserByUsername("mario");
        assertTrue(details.isAccountNonLocked());
    }

    @Test
    void loadUserByUsername_autoritaContengnoRuolo() {
        when(userService.findByUsername("mario")).thenReturn(Optional.of(localUser));
        UserDetails details = customUserDetailsService.loadUserByUsername("mario");
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("USER")));
    }
}
