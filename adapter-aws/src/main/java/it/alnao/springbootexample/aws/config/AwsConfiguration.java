package it.alnao.springbootexample.aws.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import javax.sql.DataSource;

@Configuration
@Profile("aws")
@EnableJpaRepositories(
    basePackages = "it.alnao.springbootexample.aws.repository",
    entityManagerFactoryRef = "awsEntityManagerFactory",
    transactionManagerRef = "awsTransactionManager"
)
@EntityScan(basePackages = "it.alnao.springbootexample.aws.entity")
@ComponentScan(basePackages = {
    "it.alnao.springbootexample.aws.service.auth"
})
public class AwsConfiguration {
    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties awsDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource awsDataSource() {
        return awsDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean(name = "awsEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean awsEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(awsDataSource())
                .packages("it.alnao.springbootexample.aws.entity")
                .persistenceUnit("aws")
                .build();
    }

    @Bean(name = "awsTransactionManager")
    public PlatformTransactionManager awsTransactionManager(LocalContainerEntityManagerFactoryBean awsEntityManagerFactory) {
        return new JpaTransactionManager(awsEntityManagerFactory.getObject());
    }

    @Value("${AWS_REGION:eu-central-1}")
    private String awsRegion;

    @Value("${AWS_ACCESS_KEY_ID:dummy}")
    private String awsAccessKeyId;

    @Value("${AWS_SECRET_ACCESS_KEY:dummy}")
    private String awsSecretAccessKey;

    @Value("${DYNAMODB_ENDPOINT:}")
    private String dynamodbEndpoint;

    @Bean
    public DynamoDbClient dynamoDbClient() {
        AwsCredentialsProvider credentialsProvider;
        if (awsAccessKeyId != null && !awsAccessKeyId.equals("") && awsSecretAccessKey != null && !awsSecretAccessKey.equals("")) {
            credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(awsAccessKeyId, awsSecretAccessKey));
        } else {
            credentialsProvider = DefaultCredentialsProvider.create();
        }
        // Build the client directly, chaining methods, without declaring a Builder variable
        return DynamoDbClient.builder()
            .region(Region.of(awsRegion))
            .credentialsProvider(credentialsProvider)
            .applyMutation(b -> {
                if (dynamodbEndpoint != null && !dynamodbEndpoint.isEmpty()) {
                    b.endpointOverride(java.net.URI.create(dynamodbEndpoint));
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
