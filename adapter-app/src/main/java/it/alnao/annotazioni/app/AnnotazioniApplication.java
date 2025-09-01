package it.alnao.annotazioni.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Applicazione principale Spring Boot per il sistema di gestione annotazioni
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "it.alnao.annotazioni.api",
    "it.alnao.annotazioni.port",
    "it.alnao.annotazioni.aws",
    "it.alnao.annotazioni.onprem"
})
public class AnnotazioniApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnnotazioniApplication.class, args);
    }
}
