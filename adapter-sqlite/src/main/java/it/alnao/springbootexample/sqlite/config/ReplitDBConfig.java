package it.alnao.springbootexample.sqlite.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
//import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configurazione per ReplitDB client.
 */
@Configuration
@Profile("replit")
// RIMOSSO: ora tutte le annotazioni sono su SQLite
// versione deprecata perchè replit sostituito da SQLite
public class ReplitDBConfig {
/*

    @Value("${replitdb.base-url}")
    private String replitDbBaseUrl;

    @Bean
    public WebClient replitDbWebClient() {
        return WebClient.builder()
                .baseUrl(replitDbBaseUrl)
                .build();
    }*/
}