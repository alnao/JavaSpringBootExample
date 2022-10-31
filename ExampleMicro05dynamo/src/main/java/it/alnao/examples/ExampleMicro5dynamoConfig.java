package it.alnao.examples;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;

@Configuration
//@EnableDynamoDBRepositories(basePackages = "it.alnao.examples")
@EnableDynamoDBRepositories(basePackages = "it.alnao.examples",
	includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = EnableScan.class))

public class ExampleMicro5dynamoConfig {
    @Value("${amazon.dynamodb.endpoint}")
    private String amazonDynamoDBEndpoint;
    @Value("${amazon.dynamodb.profile}")
    private String amazonDynamoDBProfile;    
    
    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        AWSCredentialsProvider credentials = 
                new ProfileCredentialsProvider(amazonDynamoDBProfile);
        AmazonDynamoDB amazonDynamoDB 
          = AmazonDynamoDBClientBuilder
               .standard()
               .withCredentials(credentials)
               .build();
        //if (!StringUtils.isEmpty(amazonDynamoDBEndpoint)) {
        //    amazonDynamoDB.setEndpoint(amazonDynamoDBEndpoint);
        //}
        return amazonDynamoDB;
    }

}