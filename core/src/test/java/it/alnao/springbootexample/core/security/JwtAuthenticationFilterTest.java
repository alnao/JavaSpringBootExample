package it.alnao.springbootexample.core.security;

import it.alnao.springbootexample.core.domain.auth.AccountType;
import it.alnao.springbootexample.core.domain.auth.User;
import it.alnao.springbootexample.core.domain.auth.UserRole;
import it.alnao.springbootexample.core.service.auth.JwtService;
import it.alnao.springbootexample.core.service.auth.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    @Mock JwtService jwtService;
    @Mock UserService userService;
    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    @Mock FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        filter = new JwtAuthenticationFilter(jwtService, userService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private User user() {
        User u = new User();
        u.setId("u-1");
        u.setUsername("mario");
        u.setRole(UserRole.USER);
        u.setAccountType(AccountType.LOCAL);
        return u;
    }

    private Authentication currentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @Test
    void doFilterInternal_withoutAuthorizationHeader_leavesTheContextEmpty() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        assertNull(currentAuthentication());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userService);
    }

    @Test
    void doFilterInternal_withANonBearerHeader_leavesTheContextEmpty() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic abc");

        filter.doFilter(request, response, filterChain);

        assertNull(currentAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withAValidToken_populatesTheSecurityContext() throws Exception {
        User user = user();
        when(request.getHeader("Authorization")).thenReturn("Bearer token-valido");
        when(jwtService.getUserIdFromToken("token-valido")).thenReturn("u-1");
        when(jwtService.getRoleFromToken("token-valido")).thenReturn("USER");
        when(userService.findById("u-1")).thenReturn(Optional.of(user));
        when(jwtService.validateToken("token-valido", user)).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        Authentication auth = currentAuthentication();
        assertNotNull(auth);
        assertSame(user, auth.getPrincipal());
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> "USER".equals(a.getAuthority())));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenTheTokenIsRejected_leavesTheContextEmpty() throws Exception {
        User user = user();
        when(request.getHeader("Authorization")).thenReturn("Bearer token-scaduto");
        when(jwtService.getUserIdFromToken("token-scaduto")).thenReturn("u-1");
        when(jwtService.getRoleFromToken("token-scaduto")).thenReturn("USER");
        when(userService.findById("u-1")).thenReturn(Optional.of(user));
        when(jwtService.validateToken("token-scaduto", user)).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertNull(currentAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenTheUserIsUnknown_leavesTheContextEmpty() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtService.getUserIdFromToken("token")).thenReturn("u-999");
        when(jwtService.getRoleFromToken("token")).thenReturn("USER");
        when(userService.findById("u-999")).thenReturn(Optional.empty());

        filter.doFilter(request, response, filterChain);

        assertNull(currentAuthentication());
        verify(jwtService, never()).validateToken(anyString(), any(User.class));
    }

    @Test
    void doFilterInternal_whenTheTokenCarriesNoUserId_leavesTheContextEmpty() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtService.getUserIdFromToken("token")).thenReturn(null);
        when(jwtService.getRoleFromToken("token")).thenReturn("USER");

        filter.doFilter(request, response, filterChain);

        assertNull(currentAuthentication());
        verifyNoInteractions(userService);
    }

    @Test
    void doFilterInternal_whenTheTokenCarriesNoRole_leavesTheContextEmpty() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtService.getUserIdFromToken("token")).thenReturn("u-1");
        when(jwtService.getRoleFromToken("token")).thenReturn("");

        filter.doFilter(request, response, filterChain);

        assertNull(currentAuthentication());
        verifyNoInteractions(userService);
    }

    @Test
    void doFilterInternal_whenTheJwtServiceThrows_stillContinuesTheChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token-corrotto");
        when(jwtService.getUserIdFromToken("token-corrotto"))
                .thenThrow(new RuntimeException("token malformato"));

        filter.doFilter(request, response, filterChain);

        assertNull(currentAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withAnEmptyBearerToken_skipsAuthentication() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        filter.doFilter(request, response, filterChain);

        assertNull(currentAuthentication());
        verifyNoInteractions(jwtService);
    }
}
