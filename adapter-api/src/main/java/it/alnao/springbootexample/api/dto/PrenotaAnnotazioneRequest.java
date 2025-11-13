package it.alnao.springbootexample.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request per prenotare un'annotazione
 */
@Schema(description = "Dati per la prenotazione di un'annotazione")
public class PrenotaAnnotazioneRequest {
    
    @NotBlank(message = "Il nome utente è obbligatorio")
    @Schema(description = "Nome dell'utente che prenota l'annotazione", example = "mario.rossi", required = true)
    private String utente;

    //parametro opzionale, valore default 60 secondi se non specificato
    @Schema(description = "Numero di secondi per la prenotazione dell'annotazione", example = "42", required = false)
    @jakarta.validation.constraints.Positive
    private Integer secondi;
    
    public PrenotaAnnotazioneRequest() {
    }
    
    public PrenotaAnnotazioneRequest(String utente, Integer secondi) {
        this.utente = utente;
        this.secondi = secondi;
    }
    
    public String getUtente() {
        return utente;
    }
    
    public void setUtente(String utente) {
        this.utente = utente;
    }
    public Integer getSecondi() {
        return secondi;
    }
    public void setSecondi(Integer secondi) {
        this.secondi = secondi;
    }
}
