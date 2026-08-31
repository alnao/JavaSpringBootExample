package it.alnao.springbootexample.aws.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.sqs.SqsClient;

import static org.junit.jupiter.api.Assertions.*;

class AwsConfigTest {

    private AwsProperties properties;

    @BeforeEach
    void setup() {
        properties = new AwsProperties();
        properties.setRegion("eu-west-1");
    }

    // ---------- AwsProperties ----------

    @Test
    void awsProperties_defaults() {
        AwsProperties p = new AwsProperties();
        assertEquals("eu-central-1", p.getRegion());
        assertNull(p.getAccessKey());
        assertNull(p.getSecretKey());
        assertEquals("annotazioni", p.getDynamodb().getAnnotazioniTableName());
        assertEquals("http://localstack:4566", p.getSqs().getEndpoint());
        assertEquals("http://localstack:4566/000000000000/annotazioni-export", p.getSqs().getExportQueueUrl());
        assertEquals("http://localstack:4566/000000000000/annotazioni-import", p.getSqs().getImportQueueUrl());
    }

    @Test
    void awsProperties_settersAndGettersWork() {
        properties.setAccessKey("AKIA-TEST");
        properties.setSecretKey("SECRET");
        assertEquals("eu-west-1", properties.getRegion());
        assertEquals("AKIA-TEST", properties.getAccessKey());
        assertEquals("SECRET", properties.getSecretKey());
    }

    @Test
    void awsProperties_nestedBlocksCanBeReplaced() {
        AwsProperties.DynamoDbConfig dynamo = new AwsProperties.DynamoDbConfig();
        dynamo.setAnnotazioniTableName("tabella-custom");
        dynamo.setEndpoint("http://localhost:8000");
        properties.setDynamodb(dynamo);

        AwsProperties.SqsConfig sqs = new AwsProperties.SqsConfig();
        sqs.setEndpoint("http://localhost:4566");
        sqs.setExportQueueUrl("http://localhost:4566/export");
        sqs.setImportQueueUrl("http://localhost:4566/import");
        properties.setSqs(sqs);

        assertEquals("tabella-custom", properties.getDynamodb().getAnnotazioniTableName());
        assertEquals("http://localhost:8000", properties.getDynamodb().getEndpoint());
        assertEquals("http://localhost:4566", properties.getSqs().getEndpoint());
        assertEquals("http://localhost:4566/export", properties.getSqs().getExportQueueUrl());
        assertEquals("http://localhost:4566/import", properties.getSqs().getImportQueueUrl());
    }

    // ---------- SqsConfig ----------

    @Test
    void sqsClient_withStaticCredentialsAndEndpointOverride() {
        properties.setAccessKey("AKIA-TEST");
        properties.setSecretKey("SECRET");
        properties.getSqs().setEndpoint("http://localhost:4566");

        try (SqsClient client = new SqsConfig(properties).sqsClient()) {
            assertNotNull(client);
        }
    }

    @Test
    void sqsClient_withoutCredentialsFallsBackToTheDefaultProvider() {
        properties.setAccessKey(null);
        properties.setSecretKey(null);
        properties.getSqs().setEndpoint("http://localhost:4566");

        try (SqsClient client = new SqsConfig(properties).sqsClient()) {
            assertNotNull(client);
        }
    }

    @Test
    void sqsClient_withEmptyCredentialsFallsBackToTheDefaultProvider() {
        properties.setAccessKey("");
        properties.setSecretKey("");
        properties.getSqs().setEndpoint("http://localhost:4566");

        try (SqsClient client = new SqsConfig(properties).sqsClient()) {
            assertNotNull(client);
        }
    }

    @Test
    void sqsClient_withoutEndpointUsesTheRegionalUrl() {
        properties.setAccessKey("AKIA-TEST");
        properties.setSecretKey("SECRET");
        properties.getSqs().setEndpoint("");

        try (SqsClient client = new SqsConfig(properties).sqsClient()) {
            assertNotNull(client);
        }
    }

    // ---------- DynamoConfig ----------

    @Test
    void dynamoDbClient_withStaticCredentialsAndEndpointOverride() {
        properties.setAccessKey("AKIA-TEST");
        properties.setSecretKey("SECRET");
        properties.getDynamodb().setEndpoint("http://localhost:8000");

        try (DynamoDbClient client = new DynamoConfig(properties).dynamoDbClient()) {
            assertNotNull(client);
        }
    }

    @Test
    void dynamoDbClient_withoutEndpointUsesTheRegionalUrl() {
        properties.setAccessKey("AKIA-TEST");
        properties.setSecretKey("SECRET");
        properties.getDynamodb().setEndpoint(null);

        try (DynamoDbClient client = new DynamoConfig(properties).dynamoDbClient()) {
            assertNotNull(client);
        }
    }

    @Test
    void dynamoDbClient_withoutCredentialsFallsBackToTheDefaultProvider() {
        properties.setAccessKey(null);
        properties.setSecretKey(null);
        properties.getDynamodb().setEndpoint("");

        try (DynamoDbClient client = new DynamoConfig(properties).dynamoDbClient()) {
            assertNotNull(client);
        }
    }

    @Test
    void dynamoDbEnhancedClient_wrapsTheLowLevelClient() {
        properties.setAccessKey("AKIA-TEST");
        properties.setSecretKey("SECRET");
        properties.getDynamodb().setEndpoint("http://localhost:8000");

        DynamoDbEnhancedClient enhanced = new DynamoConfig(properties).dynamoDbEnhancedClient();
        assertNotNull(enhanced);
    }

    @Test
    void dynamoDbClient_withOnlyTheAccessKeyFallsBackToTheDefaultProvider() {
        properties.setAccessKey("AKIA-TEST");
        properties.setSecretKey(null);
        properties.getDynamodb().setEndpoint("http://localhost:8000");

        try (DynamoDbClient client = new DynamoConfig(properties).dynamoDbClient()) {
            assertNotNull(client);
        }
    }

    @Test
    void dynamoDbClient_withAnEmptySecretFallsBackToTheDefaultProvider() {
        properties.setAccessKey("AKIA-TEST");
        properties.setSecretKey("");
        properties.getDynamodb().setEndpoint("http://localhost:8000");

        try (DynamoDbClient client = new DynamoConfig(properties).dynamoDbClient()) {
            assertNotNull(client);
        }
    }

    @Test
    void sqsClient_withOnlyTheAccessKeyFallsBackToTheDefaultProvider() {
        properties.setAccessKey("AKIA-TEST");
        properties.setSecretKey(null);
        properties.getSqs().setEndpoint("http://localhost:4566");

        try (SqsClient client = new SqsConfig(properties).sqsClient()) {
            assertNotNull(client);
        }
    }
}
