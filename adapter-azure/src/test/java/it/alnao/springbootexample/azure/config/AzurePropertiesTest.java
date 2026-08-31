package it.alnao.springbootexample.azure.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AzurePropertiesTest {

    @Test
    void cosmos_gettersAndSetters_work() {
        AzureProperties props = new AzureProperties();
        AzureProperties.CosmosConfigProperties cosmos = props.getCosmos();
        assertNotNull(cosmos);

        cosmos.setUri("https://cosmos.test");
        cosmos.setKey("key-123");
        cosmos.setDatabase("db-test");
        cosmos.setDisableSslVerification("true");

        assertEquals("https://cosmos.test", cosmos.getUri());
        assertEquals("key-123", cosmos.getKey());
        assertEquals("db-test", cosmos.getDatabase());
        assertEquals("true", cosmos.getDisableSslVerification());
    }

    @Test
    void serviceBus_gettersAndSetters_work() {
        AzureProperties props = new AzureProperties();
        AzureProperties.ServiceBusProperties sb = props.getServiceBus();
        assertNotNull(sb);

        sb.setConnectionString("Endpoint=sb://test.servicebus.windows.net/");
        sb.setQueueName("my-queue");
        sb.setImportQueueName("import-queue");

        assertEquals("Endpoint=sb://test.servicebus.windows.net/", sb.getConnectionString());
        assertEquals("my-queue", sb.getQueueName());
        assertEquals("import-queue", sb.getImportQueueName());
    }

    @Test
    void setCosmos_replacesInstance() {
        AzureProperties props = new AzureProperties();
        AzureProperties.CosmosConfigProperties newCosmos = new AzureProperties.CosmosConfigProperties();
        newCosmos.setDatabase("new-db");
        props.setCosmos(newCosmos);
        assertEquals("new-db", props.getCosmos().getDatabase());
    }

    @Test
    void setServiceBus_replacesInstance() {
        AzureProperties props = new AzureProperties();
        AzureProperties.ServiceBusProperties newSb = new AzureProperties.ServiceBusProperties();
        newSb.setQueueName("new-queue");
        props.setServiceBus(newSb);
        assertEquals("new-queue", props.getServiceBus().getQueueName());
    }
}
