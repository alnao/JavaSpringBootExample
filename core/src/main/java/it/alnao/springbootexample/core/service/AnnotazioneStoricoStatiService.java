package it.alnao.springbootexample.core.service;

import it.alnao.springbootexample.core.domain.AnnotazioneStoricoStati;

import java.util.List;

/**
 * Interfaccia per il servizio di gestione dello storico dei cambi di stato delle annotazioni
 */
public interface AnnotazioneStoricoStatiService {

    /**
     * Inserisce un nuovo record di storico cambio stato
     * 
     * @param idAnnotazione ID dell'annotazione
     * @param versione Versione dell'annotazione
     * @param statoNew Nuovo stato
     * @param statoOld Stato precedente (può essere null)
     * @param utente Utente che ha effettuato il cambio
     * @param notaOperazione Descrizione dell'operazione
     * @return Il record di storico creato
     */
    AnnotazioneStoricoStati inserisciCambioStato(String idAnnotazione, String versione, 
                                               String statoNew, String statoOld, 
                                               String utente, String notaOperazione);

    /**
     * Trova tutti i cambi di stato per una specifica annotazione
     * 
     * @param idAnnotazione ID dell'annotazione
     * @return Lista dei cambi di stato ordinati per data decrescente
     */
    List<AnnotazioneStoricoStati> trovaStoricoPerAnnotazione(String idAnnotazione);
}
