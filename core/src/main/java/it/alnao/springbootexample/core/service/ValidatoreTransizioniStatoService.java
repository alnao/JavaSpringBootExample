package it.alnao.springbootexample.core.service;

import it.alnao.springbootexample.core.domain.auth.UserRole;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import it.alnao.springbootexample.core.domain.TransizioneStato;
import it.alnao.springbootexample.core.config.TransizioniStatoConfig;
import it.alnao.springbootexample.core.exception.TransizioniStatoConfigurationException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service per validare le transizioni di stato delle annotazioni in base ai ruoli utente
 */
@Service
@EnableConfigurationProperties
public class ValidatoreTransizioniStatoService {

    private static final Logger logger = LoggerFactory.getLogger(ValidatoreTransizioniStatoService.class);

    /** Nome della risorsa di classpath che descrive le transizioni consentite. */
    private static final String RISORSA_TRANSIZIONI_DEFAULT = "cambiamentoStati.yaml";

    private final String risorsaTransizioni;

    private List<TransizioneStato> transizioniPermesse;

    public ValidatoreTransizioniStatoService() {
        this(RISORSA_TRANSIZIONI_DEFAULT);
    }

    /**
     * Costruttore con risorsa esplicita, usato dai test per verificare il
     * comportamento con configurazioni non valide.
     */
    public ValidatoreTransizioniStatoService(String risorsaTransizioni) {
        this.risorsaTransizioni = risorsaTransizioni;
    }

    /**
     * Inizializza le transizioni caricandole dal file YAML.
     * Se il caricamento non produce un insieme di transizioni valido interrompe
     * l'avvio: senza regole di transizione l'applicazione rifiuterebbe ogni
     * cambio di stato, con lo stesso codice di errore di un permesso mancante.
     */
    @PostConstruct
    public void initTransizioni() {
        logger.info("[ValidatoreTransizioniStatoService] Inizializzazione transizioni di stato da {}...", risorsaTransizioni);
        this.transizioniPermesse = caricaTransizioniDaYaml();
        logger.info("[ValidatoreTransizioniStatoService] Caricate {} transizioni di stato", transizioniPermesse.size());
    }

    /**
     * Carica le transizioni dal file YAML, senza insiemi di ripiego: le regole di
     * workflow restano descritte esclusivamente in configurazione.
     */
    private List<TransizioneStato> caricaTransizioniDaYaml() {
        TransizioniStatoConfig config = leggiConfigurazione();

        List<TransizioniStatoConfig.TransizioneYaml> voci =
                config == null ? null : config.getTransizioni();
        if (voci == null || voci.isEmpty()) {
            throw errore("non dichiara alcuna transizione: nessun cambio di stato sarebbe possibile", null);
        }

        List<TransizioneStato> transizioni = new ArrayList<>(voci.size());
        for (int i = 0; i < voci.size(); i++) {
            transizioni.add(convertiDaYaml(voci.get(i), i));
        }
        return transizioni;
    }

    /**
     * Legge e deserializza la risorsa di configurazione.
     */
    private TransizioniStatoConfig leggiConfigurazione() {
        ClassPathResource resource = new ClassPathResource(risorsaTransizioni);
        if (!resource.exists()) {
            throw errore("non è stata trovata nel classpath", null);
        }
        try (InputStream inputStream = resource.getInputStream()) {
            Yaml yaml = new Yaml();
            return yaml.loadAs(inputStream, TransizioniStatoConfig.class);
        } catch (IOException e) {
            throw errore("non è leggibile", e);
        } catch (YAMLException e) {
            throw errore("non è interpretabile come YAML valido", e);
        }
    }

    /**
     * Converte una transizione YAML in oggetto TransizioneStato.
     * L'indice e la descrizione della voce entrano nel messaggio di errore per
     * rendere identificabile la riga da correggere leggendo i soli log.
     */
    private TransizioneStato convertiDaYaml(TransizioniStatoConfig.TransizioneYaml yaml, int indice) {
        try {
            StatoAnnotazione statoPartenza = StatoAnnotazione.valueOf(yaml.getStatoPartenza());
            StatoAnnotazione statoArrivo = StatoAnnotazione.valueOf(yaml.getStatoArrivo());
            UserRole ruolo = UserRole.valueOf(yaml.getRuoloRichiesto());
            return new TransizioneStato(statoPartenza, statoArrivo, ruolo, yaml.getDescrizione());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw errore(String.format(
                    "contiene una voce non valida in posizione %d (statoPartenza=%s, statoArrivo=%s, ruoloRichiesto=%s, descrizione=%s): "
                            + "stato o ruolo non riconosciuto",
                    indice + 1, yaml.getStatoPartenza(), yaml.getStatoArrivo(),
                    yaml.getRuoloRichiesto(), yaml.getDescrizione()), e);
        }
    }

    /**
     * Costruisce l'eccezione di configurazione loggando una sola riga di errore
     * prima della terminazione, leggibile nei log di un container.
     */
    private TransizioniStatoConfigurationException errore(String motivo, Throwable causa) {
        String messaggio = String.format(
                "La configurazione delle transizioni di stato '%s' %s. "
                        + "L'applicazione non può avviarsi senza le regole di transizione.",
                risorsaTransizioni, motivo);
        logger.error("[ValidatoreTransizioniStatoService] {}", messaggio);
        return causa == null
                ? new TransizioniStatoConfigurationException(messaggio)
                : new TransizioniStatoConfigurationException(messaggio, causa);
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
