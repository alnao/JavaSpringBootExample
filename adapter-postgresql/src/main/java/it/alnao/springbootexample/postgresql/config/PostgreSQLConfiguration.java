package it.alnao.springbootexample.postgresql.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@Profile("kube")
@EnableJpaRepositories(basePackages = "it.alnao.springbootexample.postgresql.repository")
@EntityScan(basePackages = "it.alnao.springbootexample.postgresql.entity")
@EnableTransactionManagement
public class PostgreSQLConfiguration {
}
