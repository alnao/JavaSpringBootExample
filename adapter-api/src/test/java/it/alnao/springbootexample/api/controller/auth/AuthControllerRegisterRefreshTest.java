package it.alnao.springbootexample.api.controller.auth;

import it.alnao.springbootexample.api.dto.auth.JwtResponse;
import it.alnao.springbootexample.api.dto.auth.OAuth2ProviderInfo;
import it.alnao.springbootexample.api.dto.auth.RegisterRequest;
import it.alnao.springbootexample.api.dto.auth.UserProfileResponse;
import it.alnao.springbootexample.core.domain.auth.AccountType;
import it.alnao.springbootexample.core.domain.auth.User;
import it.alnao.springbootexample.core.domain.auth.UserRole;
import it.alnao.springbootexample.core.service.auth.JwtService;
import it.alnao.springbootexample.core.service.auth.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Copre registrazione, profilo, refresh e logout, non verificati in {@link AuthControllerTest}.
 */
class AuthControllerRegisterRefreshTest {

    @Mock AuthenticationManager authenticationManager;
    @Mock UserService userService;
    @Mock JwtService jwtService;
    @Mock Authentication authentication;

    private AuthController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        controller = new AuthController(authenticationManager, userService, jwtService);
    }

    private User user() {
        User u = new User();
        u.setId("u-1");
        u.setUsername("mario");
        u.setEmail("mario@test.it");
        u.setRole(UserRole.USER);
        u.setAccountType(AccountType.LOCAL);
        u.setEnabled(true);
        return u;
    }

    private RegisterRequest registerRequest() {
        return new RegisterRequest("mario", "mario@test.it", "secret");
    }

    // ---------- registerLocal ----------

    @Test
    void registerLocal_whenEmailAlreadyExists_returnsConflict() {
        when(userService.existsByEmail("mario@test.it")).thenReturn(true);
        ResponseEntity<UserProfileResponse> response = controller.registerLocal(registerRequest());
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(userService, never()).createLocalUser(anyString(), anyString(), anyString());
    }

    @Test
    void registerLocal_whenUsernameAlreadyExists_returnsConflict() {
        when(userService.existsByEmail("mario@test.it")).thenReturn(false);
        when(userService.existsByUsername("mario")).thenReturn(true);
        ResponseEntity<UserProfileResponse> response = controller.registerLocal(registerRequest());
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(userService, never()).createLocalUser(anyString(), anyString(), anyString());
    }

    @Test
    void registerLocal_whenNew_returnsCreated() {
        when(userService.existsByEmail("mario@test.it")).thenReturn(false);
        when(userService.existsByUsername("mario")).thenReturn(false);
        when(userService.createLocalUser("mario", "mario@test.it", "secret")).thenReturn(user());

        ResponseEntity<UserProfileResponse> response = controller.registerLocal(registerRequest());

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(userService).createLocalUser("mario", "mario@test.it", "secret");
    }

    @Test
    void registerLocal_appliesFirstAndLastNameWhenProvided() {
        RegisterRequest request = registerRequest();
        request.setFirstName("Mario");
        request.setLastName("Rossi");
        User created = user();
        when(userService.existsByEmail(anyString())).thenReturn(false);
        when(userService.existsByUsername(anyString())).thenReturn(false);
        when(userService.createLocalUser(anyString(), anyString(), anyString())).thenReturn(created);

        controller.registerLocal(request);

        assertEquals("Mario", created.getFirstName());
        assertEquals("Rossi", created.getLastName());
    }

    @Test
    void registerLocal_leavesNamesUntouchedWhenAbsent() {
        User created = user();
        when(userService.existsByEmail(anyString())).thenReturn(false);
        when(userService.existsByUsername(anyString())).thenReturn(false);
        when(userService.createLocalUser(anyString(), anyString(), anyString())).thenReturn(created);

        controller.registerLocal(registerRequest());

        assertNull(created.getFirstName());
        assertNull(created.getLastName());
    }

    // ---------- getCurrentUser ----------

    @Test
    void getCurrentUser_whenFound_returnsTheProfile() {
        when(authentication.getName()).thenReturn("mario");
        when(userService.findByUsername("mario")).thenReturn(Optional.of(user()));

        ResponseEntity<UserProfileResponse> response = controller.getCurrentUser(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void getCurrentUser_whenMissing_throws() {
        when(authentication.getName()).thenReturn("fantasma");
        when(userService.findByUsername("fantasma")).thenReturn(Optional.empty());

        RuntimeException e = assertThrows(RuntimeException.class,
                () -> controller.getCurrentUser(authentication));
        assertEquals("Utente non trovato", e.getMessage());
    }

    // ---------- getAvailableProviders ----------

    @Test
    void getAvailableProviders_returnsTheThreeConfiguredProviders() {
        ResponseEntity<List<OAuth2ProviderInfo>> response = controller.getAvailableProviders();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().size());
        assertEquals("google", response.getBody().get(0).getId());
        assertEquals("github", response.getBody().get(1).getId());
        assertEquals("microsoft", response.getBody().get(2).getId());
    }

    // ---------- refreshToken ----------

    @Test
    void refreshToken_whenHeaderIsNull_returnsUnauthorized() {
        assertEquals(HttpStatus.UNAUTHORIZED, controller.refreshToken(null).getStatusCode());
        verifyNoInteractions(jwtService);
    }

    @Test
    void refreshToken_whenHeaderIsNotBearer_returnsUnauthorized() {
        assertEquals(HttpStatus.UNAUTHORIZED, controller.refreshToken("Basic abc").getStatusCode());
        verifyNoInteractions(jwtService);
    }

    @Test
    void refreshToken_whenTokenIsValid_returnsANewJwt() {
        when(jwtService.refreshToken("vecchio-token")).thenReturn(Optional.of("nuovo-token"));

        ResponseEntity<JwtResponse> response = controller.refreshToken("Bearer vecchio-token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("nuovo-token", response.getBody().getToken());
        assertEquals("Bearer", response.getBody().getTokenType());
        assertEquals(86400L, response.getBody().getExpiresIn());
    }

    @Test
    void refreshToken_whenTokenIsRejected_returnsUnauthorized() {
        when(jwtService.refreshToken("scaduto")).thenReturn(Optional.empty());
        assertEquals(HttpStatus.UNAUTHORIZED, controller.refreshToken("Bearer scaduto").getStatusCode());
    }

    // ---------- logout ----------

    @Test
    void logout_whenFound_invalidatesEveryTokenOfTheUser() {
        when(authentication.getName()).thenReturn("mario");
        when(userService.findByUsername("mario")).thenReturn(Optional.of(user()));

        ResponseEntity<Void> response = controller.logout(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(jwtService).invalidateAllUserTokens("u-1");
    }

    @Test
    void logout_whenUserIsMissing_throwsAndInvalidatesNothing() {
        when(authentication.getName()).thenReturn("fantasma");
        when(userService.findByUsername("fantasma")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> controller.logout(authentication));
        verify(jwtService, never()).invalidateAllUserTokens(anyString());
    }
}
