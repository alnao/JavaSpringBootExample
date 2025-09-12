package it.alnao.springbootexample.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO per la richiesta di cambio stato di un'annotazione
 */
@Schema(description = "Richiesta di cambio stato annotazione")
public class CambiaStatoAnnotazioneRequest {
    
    @NotNull(message = "Il vecchio stato è obbligatorio")
    @Schema(description = "Stato attuale dell'annotazione", example = "INSERITA")
    private String vecchioStato;
    
    @NotNull(message = "Il nuovo stato è obbligatorio")
    @Schema(description = "Nuovo stato dell'annotazione", example = "CONFERMATA")
    private String nuovoStato;
    
    @NotBlank(message = "L'utente è obbligatorio")
    @Schema(description = "Username dell'utente che effettua il cambio", example = "admin")
    private String utente;

    // Costruttori
    public CambiaStatoAnnotazioneRequest() {}

    public CambiaStatoAnnotazioneRequest(String vecchioStato, String nuovoStato, String utente) {
        this.vecchioStato = vecchioStato;
        this.nuovoStato = nuovoStato;
        this.utente = utente;
    }

    // Getters e Setters
    public String getVecchioStato() {
        return vecchioStato;
    }

    public void setVecchioStato(String vecchioStato) {
        this.vecchioStato = vecchioStato;
    }

    public String getNuovoStato() {
        return nuovoStato;
    }

    public void setNuovoStato(String nuovoStato) {
        this.nuovoStato = nuovoStato;
    }

    public String getUtente() {
        return utente;
    }

    public void setUtente(String utente) {
        this.utente = utente;
    }

    @Override
    public String toString() {
        return "CambiaStatoAnnotazioneRequest{" +
                "vecchioStato='" + vecchioStato + '\'' +
                ", nuovoStato='" + nuovoStato + '\'' +
                ", utente='" + utente + '\'' +
                '}';
    }
}
