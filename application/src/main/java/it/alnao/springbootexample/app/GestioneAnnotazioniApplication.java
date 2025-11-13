package it.alnao.springbootexample.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
/**
 * Applicazione principale Spring Boot per il sistema di gestione Annotazioni
 */
@SpringBootApplication(exclude = {
    org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration.class,
    org.springframework.boot.actuate.autoconfigure.metrics.SystemMetricsAutoConfiguration.class
})
@EnableScheduling
@ComponentScan(basePackages = {
    "it.alnao.springbootexample.api",
    "it.alnao.springbootexample.aws",
    "it.alnao.springbootexample.azure",
    "it.alnao.springbootexample.mongodb",
    "it.alnao.springbootexample.postgresql",
    "it.alnao.springbootexample.sqlite",
    "it.alnao.springbootexample.kafka",
    "it.alnao.springbootexample.redis",
    "it.alnao.springbootexample.core"
})
public class GestioneAnnotazioniApplication {

    public static void main(String[] args) {

        
        SpringApplication.run(GestioneAnnotazioniApplication.class, args);
    }
}
