package it.alnao.springbootexample.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response per la prenotazione di un'annotazione
 */
public class PrenotaAnnotazioneResponse {
    
    private UUID annotazioneId;
    private String utente;
    private LocalDateTime dataPrenotazione;
    private LocalDateTime scadenzaLock;
    private boolean prenotataConSuccesso;
    private String messaggio;
    
    public PrenotaAnnotazioneResponse() {
    }
    
    public PrenotaAnnotazioneResponse(UUID annotazioneId, String utente, 
                                      LocalDateTime dataPrenotazione, LocalDateTime scadenzaLock,
                                      boolean prenotataConSuccesso, String messaggio) {
        this.annotazioneId = annotazioneId;
        this.utente = utente;
        this.dataPrenotazione = dataPrenotazione;
        this.scadenzaLock = scadenzaLock;
        this.prenotataConSuccesso = prenotataConSuccesso;
        this.messaggio = messaggio;
    }
    
    // Getters and Setters
    
    public UUID getAnnotazioneId() {
        return annotazioneId;
    }
    
    public void setAnnotazioneId(UUID annotazioneId) {
        this.annotazioneId = annotazioneId;
    }
    
    public String getUtente() {
        return utente;
    }
    
    public void setUtente(String utente) {
        this.utente = utente;
    }
    
    public LocalDateTime getDataPrenotazione() {
        return dataPrenotazione;
    }
    
    public void setDataPrenotazione(LocalDateTime dataPrenotazione) {
        this.dataPrenotazione = dataPrenotazione;
    }
    
    public LocalDateTime getScadenzaLock() {
        return scadenzaLock;
    }
    
    public void setScadenzaLock(LocalDateTime scadenzaLock) {
        this.scadenzaLock = scadenzaLock;
    }
    
    public boolean isPrenotataConSuccesso() {
        return prenotataConSuccesso;
    }
    
    public void setPrenotataConSuccesso(boolean prenotataConSuccesso) {
        this.prenotataConSuccesso = prenotataConSuccesso;
    }
    
    public String getMessaggio() {
        return messaggio;
    }
    
    public void setMessaggio(String messaggio) {
        this.messaggio = messaggio;
    }
}
