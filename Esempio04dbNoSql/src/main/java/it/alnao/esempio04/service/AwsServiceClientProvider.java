package it.alnao.esempio04.service;

import java.net.URI;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* questa classe non è usata ma lasciata per storia, usato direttamente il DynamoDbCOnfig */
@Service
public class AwsServiceClientProvider{
    private static final Logger logger = LoggerFactory.getLogger(AwsServiceClientProvider.class);
    

    @Value("${aws.dynamodb.endpoint:}")
    private String endpoint;

    @Value("${aws.region:eu-center-1}")
    private String region;

    @Value("${aws.accessKey:}")
    private String accessKey;

    @Value("${aws.secretKey:}")
    private String secretKey;

    private DynamoDbClient dynamoDbClient;

    public DynamoDbClient getDynamoDbClient() {
        if (Objects.isNull(dynamoDbClient)) {
            logger.info("Initializing DynamoDB client...");
            logger.info("Configuration - Region: {}, Endpoint: {}, Using custom credentials: {}", 
                        region, 
                        endpoint.isEmpty() ? "default" : endpoint,
                        !accessKey.isEmpty() && !secretKey.isEmpty());
            
            try {
                DynamoDbClientBuilder builder = DynamoDbClient.builder()
                    .region(Region.of(region)) 
                    .credentialsProvider(awsCredentialsProvider());
                
                if (!endpoint.isEmpty()) {
                    logger.info("Using custom DynamoDB endpoint: {}", endpoint);
                    builder.endpointOverride(URI.create(endpoint));
                } else {
                    logger.info("Using default AWS DynamoDB endpoint for region: {}", region);
                }
                
                dynamoDbClient = builder.build();
                logger.info("DynamoDB client initialized successfully");
                
            } catch (Exception e) {
                logger.error("Failed to initialize DynamoDB client", e);
                throw new RuntimeException("Unable to create DynamoDB client", e);
            }
        } else {
            logger.info("Returning existing DynamoDB client instance");
        }
        return dynamoDbClient;
    }
    
    private AwsCredentialsProvider awsCredentialsProvider() {
        if (!accessKey.isEmpty() && !secretKey.isEmpty()) {
            logger.info("Using static AWS credentials (access key provided)");
            logger.debug("Access key starts with: {}***", accessKey.substring(0, Math.min(4, accessKey.length())));
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey));
        } else {
            logger.info("Using default AWS credentials provider chain");
            return DefaultCredentialsProvider.create();
        }
    }
}