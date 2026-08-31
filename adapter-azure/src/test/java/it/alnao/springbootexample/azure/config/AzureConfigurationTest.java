package it.alnao.springbootexample.azure.config;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.*;

class AzureConfigurationTest {

    private AzureConfiguration configuration;

    @BeforeEach
    void setup() {
        configuration = new AzureConfiguration(new AzureProperties());
    }

    /** Sottoclasse con una datasource valida: il driver SQL Server e' sul classpath del modulo. */
    private AzureConfiguration withDataSource() {
        return new AzureConfiguration(new AzureProperties()) {
            @Override
            public DataSourceProperties azureDataSourceProperties() {
                DataSourceProperties properties = new DataSourceProperties();
                properties.setUrl("jdbc:sqlserver://localhost:1433;databaseName=test");
                properties.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                properties.setUsername("sa");
                properties.setPassword("");
                return properties;
            }
        };
    }

    @Test
    void azureDataSourceProperties_returnsAFreshInstance() {
        DataSourceProperties properties = configuration.azureDataSourceProperties();
        assertNotNull(properties);
        assertNotSame(properties, configuration.azureDataSourceProperties());
    }

    @Test
    void azureDataSource_isBuiltFromTheDataSourceProperties() {
        assertNotNull(withDataSource().azureDataSource());
    }

    @Test
    void azureEntityManagerFactory_isBuiltOnTheAzurePersistenceUnit() {
        EntityManagerFactoryBuilder builder = mock(EntityManagerFactoryBuilder.class, RETURNS_DEEP_STUBS);
        LocalContainerEntityManagerFactoryBean expected = mock(LocalContainerEntityManagerFactoryBean.class);
        when(builder.dataSource(any(DataSource.class))
                .packages("it.alnao.springbootexample.azure.entity")
                .persistenceUnit("azure")
                .build()).thenReturn(expected);

        assertSame(expected, withDataSource().azureEntityManagerFactory(builder));
    }

    @Test
    void azureTransactionManager_wrapsTheEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean emfBean = mock(LocalContainerEntityManagerFactoryBean.class);
        EntityManagerFactory emf = mock(EntityManagerFactory.class);
        when(emfBean.getObject()).thenReturn(emf);

        PlatformTransactionManager manager = configuration.azureTransactionManager(emfBean);

        assertInstanceOf(JpaTransactionManager.class, manager);
        assertSame(emf, ((JpaTransactionManager) manager).getEntityManagerFactory());
    }
}
