package it.alnao.springbootexample.aws.config;

import it.alnao.springbootexample.core.config.AnnotazioneInvioProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
@ConditionalOnProperty(value = "spring.profiles.active", havingValue = "aws")
public class SqsConfig {
    
    private final AnnotazioneInvioProperties properties;
    
    public SqsConfig(AnnotazioneInvioProperties properties) {
        this.properties = properties;
    }
    
    @Bean
    public SqsClient sqsClient() {
        return SqsClient.builder()
                .region(Region.of(properties.getSqs().getRegion()))
                .build();
    }
}
