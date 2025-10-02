package it.alnao.springbootexample.aws.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
@Profile("aws")
public class DynamoConfig {
    AwsProperties awsProperties;
    public DynamoConfig(AwsProperties awsProperties) {
        this.awsProperties = awsProperties;
    }
    
    @Bean
    public DynamoDbClient dynamoDbClient() {
        AwsCredentialsProvider credentialsProvider;
        if (awsProperties.getAccessKey() != null && !awsProperties.getAccessKey().equals("") && awsProperties.getSecretKey() != null && !awsProperties.getSecretKey().equals("")) {
            credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(awsProperties.getAccessKey(), awsProperties.getSecretKey()));
        } else {
            credentialsProvider = DefaultCredentialsProvider.create();
        }
        // Build the client directly, chaining methods, without declaring a Builder variable
        return DynamoDbClient.builder()
            .region(Region.of(awsProperties.getRegion()))
            .credentialsProvider(credentialsProvider)
            .applyMutation(b -> {
                if (awsProperties.getDynamodb().getEndpoint() != null && !awsProperties.getDynamodb().getEndpoint().isEmpty()) {
                    b.endpointOverride(java.net.URI.create(awsProperties.getDynamodb().getEndpoint()));
                }
            })
            .build();
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient() {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient())
                .build();
    }
}
