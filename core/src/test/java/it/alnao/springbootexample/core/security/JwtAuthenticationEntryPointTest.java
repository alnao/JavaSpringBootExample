package it.alnao.springbootexample.core.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import jakarta.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthenticationEntryPointTest {

    private JwtAuthenticationEntryPoint entryPoint;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setup() {
        entryPoint = new JwtAuthenticationEntryPoint();
        request = new MockHttpServletRequest("GET", "/api/annotazioni");
        response = new MockHttpServletResponse();
    }

    @Test
    void commence_returns401WithAJsonBody() throws Exception {
        AuthenticationException ex = new BadCredentialsException("Token non valido");

        entryPoint.commence(request, response, ex);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertTrue(response.getContentAsString().contains("\"error\":\"Unauthorized\""));
        assertTrue(response.getContentAsString().contains("Token non valido"));
    }

    @Test
    void commence_includesTheExceptionMessage() throws Exception {
        entryPoint.commence(request, response, new BadCredentialsException("Sessione scaduta"));
        assertTrue(response.getContentAsString().contains("Sessione scaduta"));
    }
}
