package it.alnao.esempio05.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

import java.util.List;

import org.springframework.context.annotation.Bean;  
import org.springframework.context.annotation.Configuration;

@Configuration // Indica che questa classe è una fonte di definizioni di bean
public class OpenApiConfig {
    
    @Bean
    public OpenAPI myOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost");
        devServer.setDescription("Server URL in Development environment");

        Contact contact = new Contact();
        contact.setEmail("alnao@alnao.it");
        contact.setName("Alberto Nao");
        contact.setUrl("https://www.alnao.it");

        License mitLicense = new License().name("MIT License").url("https://www.alnao.it");

        Info info = new Info()
                .title("Esempio05 testJunit5")
                .version("1.0")
                .contact(contact)
                .description("This API exposes endpoints to manage tutorials.").termsOfService("https://www.alnao.it")
                .license(mitLicense);

        return new OpenAPI().info(info).servers(List.of(devServer));
    }
/*
    @Bean // Definisce un bean che sarà gestito dal contesto Spring
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Spring Boot User & Login API") // Titolo della tua API
                        .description("API di esempio per la gestione utenti e login con Spring Boot") // Descrizione dell'API
                        .version("v0.0.1") // Versione dell'API
                        .license(new License().name("Apache 2.0").url("http://springdoc.org"))) // Informazioni sulla licenza
                .externalDocs(new ExternalDocumentation() // Documentazione esterna opzionale
                        .description("Documentazione esterna per l'API")
                        .url("https://springdoc.org/"));
    }
*/
}
