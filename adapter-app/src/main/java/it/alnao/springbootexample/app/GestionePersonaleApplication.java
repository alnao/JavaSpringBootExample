package it.alnao.springbootexample.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Applicazione principale Spring Boot per il sistema di gestione personale
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "it.alnao.springbootexample.api",
    "it.alnao.springbootexample.port",
    "it.alnao.springbootexample.aws",
    "it.alnao.springbootexample.onprem"
})
public class GestionePersonaleApplication {

    public static void main(String[] args) {
        SpringApplication.run(GestionePersonaleApplication.class, args);
    }
}
