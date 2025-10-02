package it.alnao.springbootexample.aws.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


@Configuration
@Profile("aws")
@ConfigurationProperties(prefix = "aws")
public class AwsProperties {
    
    private String region = "eu-central-1";
    private String accessKey;
    private String secretKey;
    private DynamoDbConfig dynamodb = new DynamoDbConfig();
    private SqsConfig sqs = new SqsConfig();

    // Getters and Setters
    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public DynamoDbConfig getDynamodb() {
        return dynamodb;
    }

    public void setDynamodb(DynamoDbConfig dynamodb) {
        this.dynamodb = dynamodb;
    }

    public SqsConfig getSqs() {
        return sqs;
    }

    public void setSqs(SqsConfig sqs) {
        this.sqs = sqs;
    }

    // Classe nested per configurazione DynamoDB
    public static class DynamoDbConfig {
        private String annotazioniTableName = "annotazioni";
        private String endpoint;

        public String getAnnotazioniTableName() {
            return annotazioniTableName;
        }

        public void setAnnotazioniTableName(String annotazioniTableName) {
            this.annotazioniTableName = annotazioniTableName;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }
    }

    // Classe nested per configurazione SQS
    public static class SqsConfig {
        private String endpoint = "http://localstack:4566";
        private String queueUrl = "http://localstack:4566/000000000000/annotazioni";

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getQueueUrl() {
            return queueUrl;
        }

        public void setQueueUrl(String queueUrl) {
            this.queueUrl = queueUrl;
        }

    }
}
