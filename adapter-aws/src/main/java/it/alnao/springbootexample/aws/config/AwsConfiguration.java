package it.alnao.springbootexample.aws.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
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
}
