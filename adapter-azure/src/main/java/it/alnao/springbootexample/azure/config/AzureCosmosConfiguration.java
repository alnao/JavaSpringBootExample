package it.alnao.springbootexample.azure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.core.mapping.CosmosMappingContext;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.repository.config.EnableCosmosRepositories;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.io.File;

@Configuration
@Profile("azure")
@EnableCosmosRepositories(basePackages = "it.alnao.springbootexample.azure.repository")
public class AzureCosmosConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(AzureCosmosConfiguration.class);
    private final AzureProperties azureProperties;
    
    @Autowired
    private ApplicationContext applicationContext;

    public AzureCosmosConfiguration(AzureProperties azureProperties) {
        this.azureProperties = azureProperties;
    }

    /**
     * Crea un SSLContext custom che accetta il certificato CosmosDB
     */
    private SSLContext createCustomSSLContext() {
        try {
            // Se SSL verification è disabilitata, ritorna null (usa default)
            boolean disable=Boolean.valueOf(azureProperties.getCosmos().getDisableSslVerification());
            if (disable) {
                logger.warn("[CosmosConfig] SSL verification disabled - using default SSL context");
                return null;
            }

            // Cerca il certificato CosmosDB
            File certFile = new File("/certs/cosmosdb-cert.crt");
            if (!certFile.exists()) {
                logger.warn("[CosmosConfig] Certificate not found at /certs/cosmosdb-cert.crt - using default SSL context");
                return null;
            }

            logger.info("[CosmosConfig] Loading custom certificate from: " + certFile.getAbsolutePath());
            // Carica il certificato
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate caCert;
            try (FileInputStream fis = new FileInputStream(certFile)) {
                caCert = (X509Certificate) cf.generateCertificate(fis);
            }

            // Crea un KeyStore e aggiungi il certificato
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("cosmosdb-emulator", caCert);

            // Crea un TrustManager che usa il KeyStore custom
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

            // Crea SSLContext con il TrustManager custom
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), new java.security.SecureRandom());

            logger.info("[CosmosConfig] Custom SSL context created successfully");
            return sslContext;

        } catch (Exception e) {
            logger.error("[CosmosConfig] Failed to create custom SSL context: " + e.getMessage(), e);
            return null;
        }
    }

    @Bean
    @ConditionalOnProperty(value = "azure.cosmos.enabled", havingValue = "true", matchIfMissing = false)
    public CosmosClientBuilder cosmosClientBuilder() {
        logger.info("[CosmosConfig] Creating CosmosClientBuilder with URI: " + azureProperties.getCosmos().getUri());
        
        GatewayConnectionConfig gatewayConfig = new GatewayConnectionConfig()
            .setMaxConnectionPoolSize(1000)
            .setIdleConnectionTimeout(java.time.Duration.ofSeconds(60));
        
        CosmosClientBuilder builder = new CosmosClientBuilder()
                .endpoint(azureProperties.getCosmos().getUri())
                .key(azureProperties.getCosmos().getKey())
                .gatewayMode(gatewayConfig)
                .connectionSharingAcrossClientsEnabled(true)
                .contentResponseOnWriteEnabled(true)
                .clientTelemetryEnabled(false);

        // Aggiungi SSLContext custom se disponibile
        SSLContext sslContext = createCustomSSLContext();
        if (sslContext != null) {
            // Nota: Cosmos SDK potrebbe non supportare setSslContext direttamente
            // In questo caso usa -Dio.netty.handler.ssl.noOpenSsl=true
            logger.warn("[CosmosConfig] Custom SSLContext created but may not be applied - use -Dio.netty.handler.ssl.noOpenSsl=true");
        }

        return builder;
    }

    @Bean
    @ConditionalOnProperty(value = "azure.cosmos.enabled", havingValue = "true", matchIfMissing = false)
    public CosmosAsyncClient cosmosAsyncClient(CosmosClientBuilder cosmosClientBuilder) {
        logger.info("[CosmosConfig] Creating CosmosAsyncClient");
        return cosmosClientBuilder.buildAsyncClient();
    }

    @Bean
    @ConditionalOnProperty(value = "azure.cosmos.enabled", havingValue = "true", matchIfMissing = false)
    public CosmosClient cosmosClient(CosmosClientBuilder cosmosClientBuilder) {
        logger.info("[CosmosConfig] Creating CosmosClient");
        return cosmosClientBuilder.buildClient();
    }

    @Bean
    @ConditionalOnProperty(value = "azure.cosmos.enabled", havingValue = "true", matchIfMissing = false)
    public CosmosConfig cosmosConfig() {
        logger.info("[CosmosConfig] Creating CosmosConfig");
        return CosmosConfig.builder()
                .enableQueryMetrics(true)
                .build();
    }

    @Bean
    @ConditionalOnProperty(value = "azure.cosmos.enabled", havingValue = "true", matchIfMissing = false)
    public CosmosMappingContext cosmosMappingContext() {
        logger.info("[CosmosConfig] Creating CosmosMappingContext");
        CosmosMappingContext mappingContext = new CosmosMappingContext();
        mappingContext.setApplicationContext(applicationContext);
        
        try {
            mappingContext.afterPropertiesSet();
            logger.info("[CosmosConfig] CosmosMappingContext initialized successfully");
        } catch (Exception e) {
            logger.error("[CosmosConfig] Failed to initialize CosmosMappingContext: " + e.getMessage(), e);
            throw new RuntimeException("Failed to initialize CosmosMappingContext", e);
        }
        
        return mappingContext;
    }

    @Bean
    @ConditionalOnProperty(value = "azure.cosmos.enabled", havingValue = "true", matchIfMissing = false)
    public MappingCosmosConverter mappingCosmosConverter(CosmosMappingContext cosmosMappingContext) {
        logger.info("[CosmosConfig] Creating MappingCosmosConverter");
        return new MappingCosmosConverter(cosmosMappingContext, null);
    }

    @Bean
    @ConditionalOnProperty(value = "azure.cosmos.enabled", havingValue = "true", matchIfMissing = false)
    public CosmosTemplate cosmosTemplate(
            CosmosAsyncClient cosmosAsyncClient,
            CosmosConfig cosmosConfig,
            MappingCosmosConverter mappingCosmosConverter) {
        
        logger.info("[CosmosConfig] Creating CosmosTemplate with database: " + azureProperties.getCosmos().getDatabase());
        
        CosmosTemplate template = new CosmosTemplate(
                cosmosAsyncClient,
                azureProperties.getCosmos().getDatabase(),
                cosmosConfig,
                mappingCosmosConverter
        );
        
        logger.info("[CosmosConfig] CosmosTemplate created successfully");
        return template;
    }
}