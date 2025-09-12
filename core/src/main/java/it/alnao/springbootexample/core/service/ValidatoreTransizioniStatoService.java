package it.alnao.springbootexample.core.service;

import it.alnao.springbootexample.core.domain.auth.UserRole;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import it.alnao.springbootexample.core.domain.TransizioneStato;
import it.alnao.springbootexample.core.config.TransizioniStatoConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service per validare le transizioni di stato delle annotazioni in base ai ruoli utente
 */
@Service
@EnableConfigurationProperties
public class ValidatoreTransizioniStatoService {

    private static final Logger logger = LoggerFactory.getLogger(ValidatoreTransizioniStatoService.class);

    private List<TransizioneStato> transizioniPermesse;

    /**
     * Inizializza le transizioni caricandole dal file YAML
     */
    @PostConstruct
    public void initTransizioni() {
        logger.info("Inizializzazione transizioni di stato...");
        this.transizioniPermesse = caricaTransizioniDaYaml();
        logger.info("Caricate {} transizioni di stato", transizioniPermesse.size());
    }

    /**
     * Carica le transizioni dal file YAML
     */
    private List<TransizioneStato> caricaTransizioniDaYaml() {
        try {
            ClassPathResource resource = new ClassPathResource("cambiamentoStati.yaml");
            Yaml yaml = new Yaml();
            
            try (InputStream inputStream = resource.getInputStream()) {
                TransizioniStatoConfig config = yaml.loadAs(inputStream, TransizioniStatoConfig.class);
                
                return config.getTransizioni().stream()
                    .map(this::convertiDaYaml)
                    .collect(Collectors.toList());
            }
        } catch (Exception e) {
            logger.error("Errore nel caricamento delle transizioni da YAML: {}", e.getMessage());
            logger.warn("Utilizzo transizioni di default");
            return Arrays.asList(
            // Transizioni per USER ADMIN per errore!
            new TransizioneStato(StatoAnnotazione.ERRORE, StatoAnnotazione.ERRORE, UserRole.ADMIN, 
                               "Admin può andare in errore la propria annotazione"));

        }
    }

    /**
     * Converte una transizione YAML in oggetto TransizioneStato
     */
    private TransizioneStato convertiDaYaml(TransizioniStatoConfig.TransizioneYaml yaml) {
        StatoAnnotazione statoPartenza = StatoAnnotazione.valueOf(yaml.getStatoPartenza());
        StatoAnnotazione statoArrivo = StatoAnnotazione.valueOf(yaml.getStatoArrivo());
        UserRole ruolo = UserRole.valueOf(yaml.getRuoloRichiesto());
        
        return new TransizioneStato(statoPartenza, statoArrivo, ruolo, yaml.getDescrizione());
    }


    /**
     * Verifica se una transizione di stato è permessa per un dato ruolo utente
     * 
     * @param statoAttuale Lo stato attuale dell'annotazione
     * @param nuovoStato Il nuovo stato richiesto
     * @param ruoloUtente Il ruolo dell'utente che richiede il cambio
     * @return true se la transizione è permessa, false altrimenti
     */
    public boolean isTransizionePermessa(StatoAnnotazione statoAttuale, StatoAnnotazione nuovoStato, UserRole ruoloUtente) {
        // Se non c'è cambio di stato, è sempre permesso
        if (statoAttuale == nuovoStato) {
            return true;
        }

        return transizioniPermesse.stream()
                .anyMatch(transizione -> 
                    transizione.getStatoPartenza() == statoAttuale &&
                    transizione.getStatoArrivo() == nuovoStato &&
                    hasPermissionForRole(ruoloUtente, transizione.getRuoloRichiesto())
                );
    }

    /**
     * Verifica se una transizione di stato è permessa per un dato ruolo utente (versione con stringhe)
     */
    public boolean isTransizionePermessa(String statoAttuale, String nuovoStato, String ruoloUtente) {
        try {
            StatoAnnotazione statoAttualeEnum = StatoAnnotazione.valueOf(statoAttuale);
            StatoAnnotazione nuovoStatoEnum = StatoAnnotazione.valueOf(nuovoStato);
            UserRole ruoloEnum = UserRole.valueOf(ruoloUtente);
            
            return isTransizionePermessa(statoAttualeEnum, nuovoStatoEnum, ruoloEnum);
        } catch (IllegalArgumentException e) {
            // Se uno dei valori non è valido, non permette la transizione
            return false;
        }
    }

    /**
     * Trova la transizione che corrisponde ai parametri dati
     */
    public Optional<TransizioneStato> trovaTransizione(StatoAnnotazione statoAttuale, StatoAnnotazione nuovoStato, UserRole ruoloUtente) {
        return transizioniPermesse.stream()
                .filter(transizione -> 
                    transizione.getStatoPartenza() == statoAttuale &&
                    transizione.getStatoArrivo() == nuovoStato &&
                    hasPermissionForRole(ruoloUtente, transizione.getRuoloRichiesto())
                )
                .findFirst();
    }

    /**
     * Ottiene tutte le transizioni possibili da uno stato specifico per un ruolo utente
     */
    public List<TransizioneStato> getTransizioniPossibili(StatoAnnotazione statoAttuale, UserRole ruoloUtente) {
        return transizioniPermesse.stream()
                .filter(transizione -> 
                    transizione.getStatoPartenza() == statoAttuale &&
                    hasPermissionForRole(ruoloUtente, transizione.getRuoloRichiesto())
                )
                .toList();
    }

    /**
     * Verifica se un ruolo ha i permessi per eseguire un'azione che richiede un determinato ruolo
     * Implementa una gerarchia: ADMIN > MODERATOR > USER > SYSTEM (speciale)
     */
    private boolean hasPermissionForRole(UserRole ruoloUtente, UserRole ruoloRichiesto) {
        if (ruoloUtente == ruoloRichiesto) {
            return true;
        }

        // Gerarchia dei ruoli
        switch (ruoloUtente) {
            case ADMIN:
                // Admin può fare tutto tranne operazioni di SYSTEM
                return ruoloRichiesto != UserRole.SYSTEM;
            case MODERATOR:
                // Moderator può fare operazioni di USER
                return ruoloRichiesto == UserRole.USER;
            case USER:
            case SYSTEM:
                // USER e SYSTEM possono fare solo le loro operazioni specifiche
                return false;
            default:
                return false;
        }
    }

    /**
     * Valida una transizione e lancia eccezione se non permessa
     */
    public void validaTransizione(StatoAnnotazione statoAttuale, StatoAnnotazione nuovoStato, UserRole ruoloUtente) {
        if (!isTransizionePermessa(statoAttuale, nuovoStato, ruoloUtente)) {
            throw new IllegalStateException(
                String.format("Transizione non permessa: da %s a %s per ruolo %s", 
                             statoAttuale, nuovoStato, ruoloUtente)
            );
        }
    }

    /**
     * Ottiene tutte le transizioni configurate (per debugging/admin)
     */
    public List<TransizioneStato> getTutteLeTransizioni() {
        return transizioniPermesse;
    }
}
