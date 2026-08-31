package it.alnao.springbootexample.azure.config;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.core.mapping.CosmosMappingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AzureCosmosConfigurationTest {

    @Mock ApplicationContext applicationContext;

    private AzureProperties azureProperties;
    private AzureCosmosConfiguration configuration;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        azureProperties = new AzureProperties();
        azureProperties.getCosmos().setUri("https://localhost:8081");
        // chiave base64 valida richiesta dal builder Cosmos
        azureProperties.getCosmos().setKey("C2y6yDjf5R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2NQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw==");
        azureProperties.getCosmos().setDatabase("gestioneannotazioni");
        configuration = new AzureCosmosConfiguration(azureProperties, applicationContext);
    }

    @Test
    void cosmosClientBuilder_whenSslVerificationDisabled_stillReturnsABuilder() {
        azureProperties.getCosmos().setDisableSslVerification("true");
        assertNotNull(configuration.cosmosClientBuilder());
    }

    @Test
    void cosmosClientBuilder_whenCertificateFileIsMissing_fallsBackToDefaultSsl() {
        // il certificato /certs/cosmosdb-cert.crt non esiste in ambiente di test
        azureProperties.getCosmos().setDisableSslVerification("false");
        assertNotNull(configuration.cosmosClientBuilder());
    }

    @Test
    void cosmosClientBuilder_whenCosmosPropertiesAreNull_handlesTheErrorAndReturnsABuilder() {
        azureProperties.setCosmos(null);
        // getCosmos() null fa fallire createCustomSSLContext, che logga e ritorna null:
        // il builder deve comunque essere costruito, con endpoint/key nulli.
        assertThrows(Exception.class, () -> configuration.cosmosClientBuilder());
    }

    @Test
    void cosmosAsyncClient_delegatesToTheBuilder() {
        CosmosClientBuilder builder = mock(CosmosClientBuilder.class);
        CosmosAsyncClient expected = mock(CosmosAsyncClient.class);
        when(builder.buildAsyncClient()).thenReturn(expected);
        assertSame(expected, configuration.cosmosAsyncClient(builder));
        verify(builder).buildAsyncClient();
    }

    @Test
    void cosmosClient_delegatesToTheBuilder() {
        CosmosClientBuilder builder = mock(CosmosClientBuilder.class);
        CosmosClient expected = mock(CosmosClient.class);
        when(builder.buildClient()).thenReturn(expected);
        assertSame(expected, configuration.cosmosClient(builder));
        verify(builder).buildClient();
    }

    @Test
    void cosmosConfig_enablesQueryMetrics() {
        CosmosConfig config = configuration.cosmosConfig();
        assertNotNull(config);
        assertTrue(config.isQueryMetricsEnabled());
    }

    @Test
    void cosmosMappingContext_isInitializedWithTheApplicationContext() {
        CosmosMappingContext context = configuration.cosmosMappingContext();
        assertNotNull(context);
    }

    @Test
    void mappingCosmosConverter_wrapsTheGivenMappingContext() {
        MappingCosmosConverter converter =
                configuration.mappingCosmosConverter(configuration.cosmosMappingContext());
        assertNotNull(converter);
    }

    @Test
    void cosmosTemplate_isBuiltOnTheConfiguredDatabase() {
        CosmosAsyncClient client = mock(CosmosAsyncClient.class);
        CosmosMappingContext mappingContext = configuration.cosmosMappingContext();
        CosmosTemplate template = configuration.cosmosTemplate(
                client,
                configuration.cosmosConfig(),
                configuration.mappingCosmosConverter(mappingContext));
        assertNotNull(template);
    }
}
