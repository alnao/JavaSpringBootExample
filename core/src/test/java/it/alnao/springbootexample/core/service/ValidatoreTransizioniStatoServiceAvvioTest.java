package it.alnao.springbootexample.core.service;

import it.alnao.springbootexample.core.exception.TransizioniStatoConfigurationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifica che una configurazione delle transizioni non valida impedisca il
 * completamento del contesto Spring, invece di lasciare l'applicazione avviata in
 * stato degradato.
 */
class ValidatoreTransizioniStatoServiceAvvioTest {

    @Configuration
    static class ConfigurazioneNonValida {
        @Bean
        ValidatoreTransizioniStatoService validatore() {
            return new ValidatoreTransizioniStatoService("transizioni-test/stato-inesistente.yaml");
        }
    }

    @Configuration
    static class ConfigurazioneValida {
        @Bean
        ValidatoreTransizioniStatoService validatore() {
            return new ValidatoreTransizioniStatoService("transizioni-test/valido.yaml");
        }
    }

    @Test
    void configurazioneNonValida_impedisceAvvioDelContesto() {
        BeanCreationException e = assertThrows(BeanCreationException.class, () -> {
            try (AnnotationConfigApplicationContext ctx =
                         new AnnotationConfigApplicationContext(ConfigurazioneNonValida.class)) {
                fail("il contesto non deve completare l'avvio con una configurazione non valida");
            }
        });

        Throwable causa = e;
        while (causa.getCause() != null && !(causa instanceof TransizioniStatoConfigurationException)) {
            causa = causa.getCause();
        }
        assertInstanceOf(TransizioniStatoConfigurationException.class, causa,
                "l'avvio deve fallire per la configurazione delle transizioni, non per altro");
        assertTrue(causa.getMessage().contains("posizione 2"),
                "il motivo dell'avvio fallito deve arrivare fino ai log: " + causa.getMessage());
    }

    @Test
    void configurazioneValida_completaAvvioDelContesto() {
        try (AnnotationConfigApplicationContext ctx =
                     new AnnotationConfigApplicationContext(ConfigurazioneValida.class)) {
            ValidatoreTransizioniStatoService service = ctx.getBean(ValidatoreTransizioniStatoService.class);
            assertEquals(2, service.getTutteLeTransizioni().size(),
                    "con configurazione valida il bean deve essere inizializzato dal contesto");
        }
    }
}
