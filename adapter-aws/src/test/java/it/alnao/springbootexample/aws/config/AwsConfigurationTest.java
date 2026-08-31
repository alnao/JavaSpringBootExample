package it.alnao.springbootexample.aws.config;

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

class AwsConfigurationTest {

    private AwsConfiguration configuration;

    @BeforeEach
    void setup() {
        configuration = new AwsConfiguration();
    }

    @Test
    void awsDataSourceProperties_returnsAFreshInstance() {
        DataSourceProperties properties = configuration.awsDataSourceProperties();
        assertNotNull(properties);
        assertNotSame(properties, configuration.awsDataSourceProperties());
    }

    @Test
    void awsDataSource_isBuiltFromTheDataSourceProperties() {
        AwsConfiguration withUrl = new AwsConfiguration() {
            @Override
            public DataSourceProperties awsDataSourceProperties() {
                DataSourceProperties properties = new DataSourceProperties();
                properties.setUrl("jdbc:mysql://localhost:3306/test");
                properties.setDriverClassName("com.mysql.cj.jdbc.Driver");
                properties.setUsername("sa");
                properties.setPassword("");
                return properties;
            }
        };

        DataSource dataSource = withUrl.awsDataSource();

        assertNotNull(dataSource);
    }

    @Test
    void awsEntityManagerFactory_isBuiltOnTheAwsPersistenceUnit() {
        EntityManagerFactoryBuilder builder = mock(EntityManagerFactoryBuilder.class, RETURNS_DEEP_STUBS);
        LocalContainerEntityManagerFactoryBean expected = mock(LocalContainerEntityManagerFactoryBean.class);
        when(builder.dataSource(any(DataSource.class))
                .packages("it.alnao.springbootexample.aws.entity")
                .persistenceUnit("aws")
                .build()).thenReturn(expected);

        AwsConfiguration withUrl = new AwsConfiguration() {
            @Override
            public DataSourceProperties awsDataSourceProperties() {
                DataSourceProperties properties = new DataSourceProperties();
                properties.setUrl("jdbc:mysql://localhost:3306/test");
                properties.setDriverClassName("com.mysql.cj.jdbc.Driver");
                return properties;
            }
        };

        assertSame(expected, withUrl.awsEntityManagerFactory(builder));
    }

    @Test
    void awsTransactionManager_wrapsTheEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean emfBean = mock(LocalContainerEntityManagerFactoryBean.class);
        EntityManagerFactory emf = mock(EntityManagerFactory.class);
        when(emfBean.getObject()).thenReturn(emf);

        PlatformTransactionManager manager = configuration.awsTransactionManager(emfBean);

        assertInstanceOf(JpaTransactionManager.class, manager);
        assertSame(emf, ((JpaTransactionManager) manager).getEntityManagerFactory());
    }
}
