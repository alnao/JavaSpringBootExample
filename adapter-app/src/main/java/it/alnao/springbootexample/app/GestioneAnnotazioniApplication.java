package it.alnao.springbootexample.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
/**
 * Applicazione principale Spring Boot per il sistema di gestione Annotazioni
 */
@SpringBootApplication(exclude = {
    org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration.class,
    org.springframework.boot.actuate.autoconfigure.metrics.SystemMetricsAutoConfiguration.class
})
@ComponentScan(basePackages = {
    "it.alnao.springbootexample.api",
    "it.alnao.springbootexample.port",
    "it.alnao.springbootexample.aws",
    "it.alnao.springbootexample.onprem",
    "it.alnao.springbootexample.sqlite"
})
public class GestioneAnnotazioniApplication {

    public static void main(String[] args) {

        
        SpringApplication.run(GestioneAnnotazioniApplication.class, args);
    }
}
