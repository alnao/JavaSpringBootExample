package it.alnao.springbootexample.azure.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.lang.reflect.Constructor;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SslDisablerInitializerTest {

    @Mock ConfigurableApplicationContext applicationContext;
    @Mock ConfigurableEnvironment environment;

    private SslDisablerInitializer initializer;

    // stato SSL globale da ripristinare: il disabler lo modifica a livello di JVM
    private SSLContext defaultSslContext;
    private SSLSocketFactory defaultSocketFactory;
    private HostnameVerifier defaultHostnameVerifier;

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.openMocks(this);
        initializer = new SslDisablerInitializer();
        when(applicationContext.getEnvironment()).thenReturn(environment);
        defaultSslContext = SSLContext.getDefault();
        defaultSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
        defaultHostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
    }

    @AfterEach
    void restoreSslDefaults() {
        SSLContext.setDefault(defaultSslContext);
        HttpsURLConnection.setDefaultSSLSocketFactory(defaultSocketFactory);
        HttpsURLConnection.setDefaultHostnameVerifier(defaultHostnameVerifier);
    }

    @Test
    void initialize_whenAzureProfileIsNotActive_doesNothing() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"sqlite"});
        initializer.initialize(applicationContext);
        verify(environment, never()).getProperty("AZURE_COSMOS_DISABLE_SSL_VERIFICATION");
    }

    @Test
    void initialize_whenNoProfileIsActive_doesNothing() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{});
        initializer.initialize(applicationContext);
        verify(environment, never()).getProperty("AZURE_COSMOS_DISABLE_SSL_VERIFICATION");
    }

    @Test
    void initialize_whenFlagIsNotTrue_leavesSslUntouched() throws Exception {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"azure"});
        when(environment.getProperty("AZURE_COSMOS_DISABLE_SSL_VERIFICATION")).thenReturn("false");
        SSLContext before = SSLContext.getDefault();
        initializer.initialize(applicationContext);
        assertSame(before, SSLContext.getDefault());
    }

    @Test
    void initialize_whenFlagIsMissing_leavesSslUntouched() throws Exception {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"azure"});
        when(environment.getProperty("AZURE_COSMOS_DISABLE_SSL_VERIFICATION")).thenReturn(null);
        SSLContext before = SSLContext.getDefault();
        initializer.initialize(applicationContext);
        assertSame(before, SSLContext.getDefault());
    }

    @Test
    void initialize_whenAzureProfileAndFlagTrue_disablesVerificationAndSetsSystemProperties() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"azure", "web"});
        when(environment.getProperty("AZURE_COSMOS_DISABLE_SSL_VERIFICATION")).thenReturn("TRUE");

        initializer.initialize(applicationContext);

        assertEquals("TLSv1.2,TLSv1.3", System.getProperty("jdk.tls.client.protocols"));
        assertEquals("TLSv1.2,TLSv1.3", System.getProperty("https.protocols"));
        assertEquals("true", System.getProperty("jdk.internal.httpclient.disableHostnameVerification"));
        assertEquals("true", System.getProperty("io.netty.handler.ssl.noOpenSsl"));
        assertEquals("false", System.getProperty("io.netty.ssl.noUnsafeMdAlgorithms"));
        assertTrue(HttpsURLConnection.getDefaultHostnameVerifier().verify("qualsiasi-host", null));
    }

    @Test
    void azureProfileConstant_isTheExpectedOne() {
        assertEquals("azure", SslDisablerInitializer.AZURE_PROFILE);
    }

    /**
     * Il TrustManager permissivo e' una classe anonima interna a disableSSLVerification:
     * non e' raggiungibile dall'API pubblica, quindi lo si individua tra le classi
     * annidate cercando quella che implementa X509TrustManager (piu' robusto che
     * affidarsi al nome sintetico "$1").
     */
    private X509TrustManager trustManagerPermissivo() throws Exception {
        for (int i = 1; i <= 5; i++) {
            Class<?> candidata;
            try {
                candidata = Class.forName(SslDisablerInitializer.class.getName() + "$" + i);
            } catch (ClassNotFoundException e) {
                continue;
            }
            if (X509TrustManager.class.isAssignableFrom(candidata)) {
                Constructor<?> ctor = candidata.getDeclaredConstructor();
                ctor.setAccessible(true);
                return (X509TrustManager) ctor.newInstance();
            }
        }
        throw new IllegalStateException("TrustManager anonimo non trovato in SslDisablerInitializer");
    }

    @Test
    void trustManager_accettaQualsiasiCertificato() throws Exception {
        X509TrustManager manager = trustManagerPermissivo();

        assertNull(manager.getAcceptedIssuers());
        assertDoesNotThrow(() -> manager.checkClientTrusted(new X509Certificate[0], "RSA"));
        assertDoesNotThrow(() -> manager.checkServerTrusted(new X509Certificate[0], "RSA"));
    }
}
