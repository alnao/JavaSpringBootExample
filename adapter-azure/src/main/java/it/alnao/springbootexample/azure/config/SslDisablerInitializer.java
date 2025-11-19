package it.alnao.springbootexample.azure.config;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;

/**
 * Initializer eseguito PRIMA di qualsiasi bean Spring.
 * Disabilita la verifica SSL per l'emulatore CosmosDB.
 * ⚠️ ATTENZIONE: Usare SOLO in sviluppo, MAI in produzione!
 */
@Profile("azure")
public class SslDisablerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final Logger logger = LoggerFactory.getLogger(SslDisablerInitializer.class);
    public static final String AZURE_PROFILE = "azure";

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        // Controlla se il profilo "azure" è attivo
        String[] activeProfiles = applicationContext.getEnvironment().getActiveProfiles();
        boolean isAzureProfile = Arrays.asList(activeProfiles).contains(AZURE_PROFILE);
        
        if (!isAzureProfile) {
            logger.info("Profilo Azure non attivo, skip SSL disabler");
            return;
        }
        
        // Controlla se la variabile d'ambiente è impostata
        String disableSSL = applicationContext.getEnvironment().getProperty("AZURE_COSMOS_DISABLE_SSL_VERIFICATION");
        if (!"true".equalsIgnoreCase(disableSSL)) {
            logger.info("AZURE_COSMOS_DISABLE_SSL_VERIFICATION non è 'true', skip SSL disabler");
            return;
        }
        
        logger.warn("WARNING: Disabling SSL verification for CosmosDB (Development only!)");
        disableSSLVerification();
    }

    private static void disableSSLVerification() {
        try {
            // 1. TrustManager che accetta tutti i certificati
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
            };

            // 2. Configura SSLContext
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            
            // 3. Disabilita verifica hostname
            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
            
            // 4. Imposta SSLContext di default (usato anche da Netty se non usa OpenSSL)
            SSLContext.setDefault(sc);
            
            // 5. System properties per Java SSL
            System.setProperty("jdk.tls.client.protocols", "TLSv1.2,TLSv1.3");
            System.setProperty("https.protocols", "TLSv1.2,TLSv1.3");
            System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");
            
            // 6. CRITICO: Forza Netty a usare JDK SSL invece di OpenSSL nativo
            System.setProperty("io.netty.handler.ssl.noOpenSsl", "true");
            
            // 7. Disabilita validazione certificati in Netty
            System.setProperty("io.netty.ssl.noUnsafeMdAlgorithms", "false");
            
            logger.info("SSL verification disabled successfully");
            logger.info("   - Profilo: azure");
            logger.info("   - SSLContext.setDefault() configurato");
            logger.info("   - HostnameVerifier disabilitato");
            logger.info("   - Netty forzato a usare JDK SSL (noOpenSsl=true)");
            logger.info("   - System properties SSL configurate");
            
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            logger.error("Errore durante la disabilitazione della verifica SSL: " + e.getMessage(), e);
            throw new RuntimeException("Errore durante la disabilitazione della verifica SSL", e);
        }
    }
}