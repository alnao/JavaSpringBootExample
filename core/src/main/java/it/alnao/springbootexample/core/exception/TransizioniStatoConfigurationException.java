package it.alnao.springbootexample.core.exception;

/**
 * Eccezione lanciata quando la configurazione delle transizioni di stato non è
 * caricabile: file assente, contenuto non interpretabile, riferimento a uno stato
 * o a un ruolo non riconosciuto, oppure nessuna transizione dichiarata.
 *
 * Viene sollevata durante l'inizializzazione del bean e impedisce il
 * completamento del contesto Spring: senza le regole di transizione
 * l'applicazione non è in grado di gestire il ciclo di vita delle annotazioni.
 */
public class TransizioniStatoConfigurationException extends RuntimeException {

    public TransizioniStatoConfigurationException(String message) {
        super(message);
    }

    public TransizioniStatoConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
