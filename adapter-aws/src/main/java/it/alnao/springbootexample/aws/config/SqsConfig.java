package it.alnao.springbootexample.aws.config;

//import it.alnao.springbootexample.core.config.AnnotazioneInvioProperties;

import java.net.URI;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

@Configuration
@ConditionalOnProperty(value = "spring.profiles.active", havingValue = "aws")
public class SqsConfig {
    
    //private final AnnotazioneInvioProperties properties;
    private final AwsProperties awsProperties;

    public SqsConfig(AwsProperties awsProperties) {
        this.awsProperties = awsProperties;
    }
    
    @Bean
    public SqsClient sqsClient() {
        AwsCredentialsProvider credentialsProvider;
        if (awsProperties.getAccessKey() != null && !awsProperties.getAccessKey().equals("") && awsProperties.getSecretKey() != null && !awsProperties.getSecretKey().equals("")) {
            credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(awsProperties.getAccessKey(), awsProperties.getSecretKey()));
        } else {
            credentialsProvider = DefaultCredentialsProvider.create();
        }        
        SqsClientBuilder builder = SqsClient.builder()
            .region(Region.of(awsProperties.getRegion()))
            .credentialsProvider(credentialsProvider);

        if (!awsProperties.getSqs().getEndpoint().isEmpty()) {
            builder.endpointOverride(URI.create(awsProperties.getSqs().getEndpoint()));
        }
        
        return builder.build();

        //return SqsClient.builder()
        //        .region(Region.of(properties.getSqs().getRegion()))
        //        .build();
    }
}
