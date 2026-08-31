package it.alnao.springbootexample.api.controller.auth;

import it.alnao.springbootexample.api.dto.auth.JwtResponse;
import it.alnao.springbootexample.api.dto.auth.LoginRequest;
import it.alnao.springbootexample.api.dto.auth.RegisterRequest;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock AuthenticationManager authenticationManager;
    @Mock UserService userService;
    @Mock JwtService jwtService;

    AuthController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        controller = new AuthController(authenticationManager, userService, jwtService);
    }

    @Test
    void loginLocal_withValidCredentials_returnsJwt() {
        User user = buildUser("user1");
        user.setAccountType(AccountType.LOCAL);
        LoginRequest req = new LoginRequest();
        req.setUsername("user1");
        req.setPassword("pass");

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userService.findByUsername("user1")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        ResponseEntity<?> resp = controller.loginLocal(req);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        verify(userService).updateLastLogin(any());
    }

    @Test
    void loginLocal_withWrongCredentials_returnsUnauthorized() {
        LoginRequest req = new LoginRequest();
        req.setUsername("user1");
        req.setPassword("wrong");

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        ResponseEntity<?> resp = controller.loginLocal(req);

        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
    }

    @Test
    void loginLocal_withExternalAccountType_returnsUnauthorized() {
        User user = buildUser("user1");
        user.setAccountType(AccountType.GOOGLE);
        LoginRequest req = new LoginRequest();
        req.setUsername("user1");
        req.setPassword("pass");

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userService.findByUsername("user1")).thenReturn(Optional.of(user));

        ResponseEntity<?> resp = controller.loginLocal(req);

        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
    }

    @Test
    void getAvailableProviders_returnsOk() {
        ResponseEntity<?> resp = controller.getAvailableProviders();
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    private User buildUser(String username) {
        User u = new User();
        u.setId("id-1");
        u.setUsername(username);
        u.setEmail(username + "@test.com");
        u.setPassword("pass");
        u.setRole(UserRole.USER);
        u.setAccountType(AccountType.LOCAL);
        u.setEnabled(true);
        return u;
    }
}
