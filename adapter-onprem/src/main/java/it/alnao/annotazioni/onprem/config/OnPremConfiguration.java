package it.alnao.annotazioni.onprem.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@Profile("onprem")
@EnableJpaRepositories(basePackages = "it.alnao.annotazioni.onprem.repository")
@EnableMongoRepositories(basePackages = "it.alnao.annotazioni.onprem.repository")
@EntityScan(basePackages = "it.alnao.annotazioni.onprem.entity")
@EnableTransactionManagement
public class OnPremConfiguration {
}
