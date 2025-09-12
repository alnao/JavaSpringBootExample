package it.alnao.springbootexample.core.service;

import it.alnao.springbootexample.core.domain.Annotazione;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta per il servizio di gestione delle annotazioni
 */
public interface AnnotazioneService {
    
    /**
     * Crea una nuova annotazione completa
     */
    AnnotazioneCompleta creaAnnotazione(String valoreNota, String descrizione, String utente);
    
    /**
     * Aggiorna un'annotazione esistente
     */
    AnnotazioneCompleta aggiornaAnnotazione(UUID id, String nuovoValore, String nuovaDescrizione, String utenteModifica);


    /**
     * Aggiorna lo stato di un'annotazione esistente
     */
    AnnotazioneCompleta cambiaStato(UUID id, String nuovoStato, String utenteModifica);

    /**
     * Trova un'annotazione completa per ID
     */
    Optional<AnnotazioneCompleta> trovaPerID(UUID id);
    
    /**
     * Trova tutte le annotazioni complete
     */
    List<AnnotazioneCompleta> trovaTutte();
    
    /**
     * Trova annotazioni per utente
     */
    List<AnnotazioneCompleta> trovaPerUtente(String utente);
    
    /**
     * Trova annotazioni per categoria
     */
    List<AnnotazioneCompleta> trovaPerCategoria(String categoria);
    
    /**
     * Trova annotazioni create in un periodo
     */
    List<AnnotazioneCompleta> trovaPerPeriodo(LocalDateTime inizio, LocalDateTime fine);
    
    /**
     * Cerca annotazioni per testo (nel valore o nella descrizione)
     */
    List<AnnotazioneCompleta> cercaPerTesto(String testo);
    
    /**
     * Trova annotazioni pubbliche
     */
    List<AnnotazioneCompleta> trovaPubbliche();
    
    /**
     * Trova annotazioni per stato
     */
    List<AnnotazioneCompleta> trovaPerStato(StatoAnnotazione stato);
    
    /**
     * Elimina un'annotazione
     */
    void eliminaAnnotazione(UUID id);
    
    /**
     * Verifica se un'annotazione esiste
     */
    boolean esisteAnnotazione(UUID id);
    
    /**
     * Conta le annotazioni totali
     */
    long contaAnnotazioni();
    
    /**
     * Conta le annotazioni per utente
     */
    long contaAnnotazioniPerUtente(String utente);
    
    /**
     * Imposta la visibilità pubblica di un'annotazione
     */
    void impostaVisibilitaPubblica(UUID id, boolean pubblica, String utente);
    
    /**
     * Imposta la categoria di un'annotazione
     */
    void impostaCategoria(UUID id, String categoria, String utente);
    
    /**
     * Imposta i tag di un'annotazione
     */
    void impostaTags(UUID id, String tags, String utente);
    
    /**
     * Imposta la priorità di un'annotazione
     */
    void impostaPriorita(UUID id, Integer priorita, String utente);
}
