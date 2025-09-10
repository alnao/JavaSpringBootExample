package it.alnao.springbootexample.sqlite.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.boot.autoconfigure.domain.EntityScan;

/**
 * Configurazione principale per il profilo sqlite.
 * Abilita la scansione dei componenti e i repository JPA per SQLite.
 */
@Configuration
@Profile("sqlite")
@ComponentScan(basePackages = "it.alnao.springbootexample.sqlite")
@EnableJpaRepositories(basePackages = {"it.alnao.springbootexample.sqlite.repository"})
@EntityScan(basePackages = "it.alnao.springbootexample.sqlite.entity")
@EnableTransactionManagement
public class AdapterSqlLiteConfiguration {
    
    // Configurazione automatica tramite annotations
    // I bean per ReplitDB sono definiti in ReplitDBConfig dismesso
    // I repository SQLite sono configurati tramite @EnableJpaRepositories
}
