package it.alnao.springbootexample.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO per rappresentare una transizione di stato nelle risposte API
 */
@Schema(description = "Rappresenta una transizione di stato permessa con le regole di autorizzazione")
public class TransizioneStatoResponse {

    @Schema(description = "Stato di partenza dell'annotazione", example = "INSERITA")
    private String statoPartenza;

    @Schema(description = "Stato di arrivo dell'annotazione", example = "MODIFICATA")
    private String statoArrivo;

    @Schema(description = "Ruolo utente richiesto per questa transizione", example = "USER")
    private String ruoloRichiesto;

    @Schema(description = "Descrizione della transizione", example = "Utente può modificare la propria annotazione")
    private String descrizione;

    public TransizioneStatoResponse() {
    }

    public TransizioneStatoResponse(String statoPartenza, String statoArrivo, String ruoloRichiesto, String descrizione) {
        this.statoPartenza = statoPartenza;
        this.statoArrivo = statoArrivo;
        this.ruoloRichiesto = ruoloRichiesto;
        this.descrizione = descrizione;
    }

    public String getStatoPartenza() {
        return statoPartenza;
    }

    public void setStatoPartenza(String statoPartenza) {
        this.statoPartenza = statoPartenza;
    }

    public String getStatoArrivo() {
        return statoArrivo;
    }

    public void setStatoArrivo(String statoArrivo) {
        this.statoArrivo = statoArrivo;
    }

    public String getRuoloRichiesto() {
        return ruoloRichiesto;
    }

    public void setRuoloRichiesto(String ruoloRichiesto) {
        this.ruoloRichiesto = ruoloRichiesto;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }
}
