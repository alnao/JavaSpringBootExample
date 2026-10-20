package it.alnao.springbootexample.core.config.init;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Arrays;
import java.util.List;

/**
 * Interrompe l'avvio se manca una delle credenziali elencate in app.credenziali.obbligatorie
 * (nomi di variabili d'ambiente, dichiarati dal profilo: per kube POSTGRES_PASSWORD e MONGODB_URI).
 * Gira prima della creazione del contesto, quindi l'errore arriva con il nome della variabile e non
 * come autenticazione fallita verso il database. Il messaggio riporta solo i nomi, mai i valori.
 * Registrato in META-INF/spring.factories; ordine piu' basso per girare dopo il caricamento degli yml
 * (change openspec secret-kube, design D2).
 */
public class CredenzialiObbligatoriePostProcessor implements EnvironmentPostProcessor, Ordered {

    static final String PROPRIETA = "app.credenziali.obbligatorie";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String[] nomi = environment.getProperty(PROPRIETA, String[].class, new String[0]);
        List<String> mancanti = Arrays.stream(nomi)
                .map(String::trim)
                .filter(nome -> !nome.isEmpty())
                .filter(nome -> {
                    String valore = environment.getProperty(nome);
                    return valore == null || valore.isBlank();
                })
                .toList();
        if (!mancanti.isEmpty()) {
            throw new IllegalStateException("Credenziale mancante: " + String.join(", ", mancanti));
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
