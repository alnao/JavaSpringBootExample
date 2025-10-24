package it.alnao.springbootexample.azure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("azure")
@ConfigurationProperties(prefix = "azure")
public class AzureProperties {
    
    private CosmosConfigProperties cosmos = new CosmosConfigProperties();
    private ServiceBusProperties serviceBus = new ServiceBusProperties();



    // Getters and Setters
    public CosmosConfigProperties getCosmos() {
        return cosmos;
    }

    public void setCosmos(CosmosConfigProperties cosmos) {
        this.cosmos = cosmos;
    }

    public ServiceBusProperties getServiceBus() {
        return serviceBus;
    }
    public void setServiceBus(ServiceBusProperties serviceBus) {
        this.serviceBus = serviceBus;
    }

    public static class ServiceBusProperties {
        private String connectionString = "Endpoint=sb://your-servicebus-namespace.servicebus.windows.net/;SharedAccessKeyName=your-key-name;SharedAccessKey=your-key";
        private String queueName = "annotazioni-queue";

        // Getters and Setters
        public String getConnectionString() {
            return connectionString;
        }

        public void setConnectionString(String connectionString) {
            this.connectionString = connectionString;
        }

        public String getQueueName() {
            return queueName;
        }

        public void setQueueName(String queueName) {
            this.queueName = queueName;
        }
    }


    // Classe nested per configurazione CosmosDB
    public static class CosmosConfigProperties {
        private String uri = "https://localhost:8081";
        private String key = "xxxxx";
        private String database = "gestioneannotazioni";
        private String disableSslVerification = "false";

        // Getters and Setters
        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getDatabase() {
            return database;
        }

        public void setDatabase(String database) {
            this.database = database;
        }

        public String getDisableSslVerification() {
            return disableSslVerification;
        }

        public void setDisableSslVerification(String disableSslVerification) {
            this.disableSslVerification = disableSslVerification;
        }

    }
}
