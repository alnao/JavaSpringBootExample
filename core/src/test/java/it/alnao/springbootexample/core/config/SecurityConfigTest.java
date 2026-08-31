package it.alnao.springbootexample.core.config;

import it.alnao.springbootexample.core.security.JwtAuthenticationEntryPoint;
import it.alnao.springbootexample.core.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Answers.RETURNS_SELF;
import static org.mockito.Mockito.*;

class SecurityConfigTest {

    @Mock JwtAuthenticationEntryPoint entryPoint;
    @Mock JwtAuthenticationFilter jwtAuthenticationFilter;

    private SecurityConfig config;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        config = new SecurityConfig(entryPoint);
        ReflectionTestUtils.setField(config, "corsAllowedOrigins",
                List.of("http://localhost:8082", "https://alnao.it:8082"));
    }

    @Test
    void authenticationManager_delegatesToTheSpringConfiguration() throws Exception {
        AuthenticationConfiguration authConfig = mock(AuthenticationConfiguration.class);
        AuthenticationManager expected = mock(AuthenticationManager.class);
        when(authConfig.getAuthenticationManager()).thenReturn(expected);

        assertSame(expected, config.authenticationManager(authConfig));
    }

    @Test
    void corsConfigurationSource_allowsTheConfiguredOriginsAndMethods() {
        CorsConfigurationSource source = config.corsConfigurationSource();
        CorsConfiguration cors = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/annotazioni"));

        assertNotNull(cors);
        assertEquals(List.of("http://localhost:8082", "https://alnao.it:8082"), cors.getAllowedOriginPatterns());
        assertTrue(cors.getAllowedMethods().containsAll(
                List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")));
        assertEquals(List.of("*"), cors.getAllowedHeaders());
        assertEquals(Boolean.TRUE, cors.getAllowCredentials());
    }

    @Test
    void corsConfigurationSource_appliesToEveryPath() {
        CorsConfigurationSource source = config.corsConfigurationSource();
        assertNotNull(source.getCorsConfiguration(new MockHttpServletRequest("POST", "/qualsiasi/path")));
    }

    @Test
    void filterChain_registersTheJwtFilterAndBuildsTheChain() throws Exception {
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        DefaultSecurityFilterChain built = mock(DefaultSecurityFilterChain.class);
        when(http.build()).thenReturn(built);

        SecurityFilterChain chain = config.filterChain(http, jwtAuthenticationFilter);

        assertSame(built, chain);
        verify(http).addFilterBefore(eq(jwtAuthenticationFilter), any());
        verify(http).build();
    }

    @Test
    @SuppressWarnings("unchecked")
    void filterChain_appliesEveryCustomizerOnTheHttpSecurityDsl() throws Exception {
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_SELF);
        when(http.build()).thenReturn(mock(DefaultSecurityFilterChain.class));

        config.filterChain(http, jwtAuthenticationFilter);

        // Le lambda passate al DSL non vengono eseguite dai mock: le invochiamo a mano
        // per verificare che il corpo di ciascuna configurazione sia effettivamente eseguibile.
        ArgumentCaptor<Customizer<CorsConfigurer<HttpSecurity>>> cors =
                ArgumentCaptor.forClass(Customizer.class);
        verify(http).cors(cors.capture());
        assertDoesNotThrow(() -> cors.getValue().customize(mock(CorsConfigurer.class, RETURNS_DEEP_STUBS)));

        ArgumentCaptor<Customizer<CsrfConfigurer<HttpSecurity>>> csrf =
                ArgumentCaptor.forClass(Customizer.class);
        verify(http).csrf(csrf.capture());
        assertDoesNotThrow(() -> csrf.getValue().customize(mock(CsrfConfigurer.class, RETURNS_DEEP_STUBS)));

        ArgumentCaptor<Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry>> authz =
                ArgumentCaptor.forClass(Customizer.class);
        verify(http).authorizeHttpRequests(authz.capture());
        assertDoesNotThrow(() -> authz.getValue().customize(
                mock(AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry.class,
                     RETURNS_DEEP_STUBS)));

        ArgumentCaptor<Customizer<ExceptionHandlingConfigurer<HttpSecurity>>> ex =
                ArgumentCaptor.forClass(Customizer.class);
        verify(http).exceptionHandling(ex.capture());
        assertDoesNotThrow(() -> ex.getValue().customize(mock(ExceptionHandlingConfigurer.class, RETURNS_DEEP_STUBS)));

        ArgumentCaptor<Customizer<SessionManagementConfigurer<HttpSecurity>>> session =
                ArgumentCaptor.forClass(Customizer.class);
        verify(http).sessionManagement(session.capture());
        assertDoesNotThrow(() -> session.getValue().customize(mock(SessionManagementConfigurer.class, RETURNS_DEEP_STUBS)));

        ArgumentCaptor<Customizer<OAuth2LoginConfigurer<HttpSecurity>>> oauth2 =
                ArgumentCaptor.forClass(Customizer.class);
        verify(http).oauth2Login(oauth2.capture());
        assertDoesNotThrow(() -> oauth2.getValue().customize(mock(OAuth2LoginConfigurer.class, RETURNS_DEEP_STUBS)));
    }
}
