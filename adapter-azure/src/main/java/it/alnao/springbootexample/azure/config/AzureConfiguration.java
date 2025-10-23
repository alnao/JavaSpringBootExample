package it.alnao.springbootexample.azure.config;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Profile("azure")
@EnableJpaRepositories(
    basePackages = {"it.alnao.springbootexample.azure.repository","it.alnao.springbootexample.azure.repository.auth"},
    entityManagerFactoryRef = "azureEntityManagerFactory",
    transactionManagerRef = "azureTransactionManager"
)
@EntityScan(basePackages = "it.alnao.springbootexample.azure.entity")
@ComponentScan(basePackages = {
    "it.alnao.springbootexample.azure.service","it.alnao.springbootexample.azure.service.auth"
})
public class AzureConfiguration {
    //private final AzureProperties azureProperties;
    public AzureConfiguration(AzureProperties azureProperties) {
    //    this.azureProperties = azureProperties;
    //     System.out.println("AzureConfiguration initialized");
    }

    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties azureDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource azureDataSource() {
        return azureDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean(name = "azureEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean azureEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(azureDataSource())
                .packages("it.alnao.springbootexample.azure.entity")
                .persistenceUnit("azure")
                .build();
    }

    @Bean(name = "azureTransactionManager")
    public PlatformTransactionManager azureTransactionManager(LocalContainerEntityManagerFactoryBean azureEntityManagerFactory) {
        return new JpaTransactionManager(azureEntityManagerFactory.getObject());
    }

//    @Bean
//    public String azureBlobStorageConnectionString() {
//        return "DefaultEndpointsProtocol=https;AccountName=your_account_name;AccountKey=your_account_key;EndpointSuffix=core.windows.net";
//    }

}
