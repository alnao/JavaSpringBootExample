package it.alnao.springbootexample.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO per la risposta delle annotazioni
 */
public class AnnotazioneResponse {
    
    private UUID id;
    private String versioneNota;
    private String valoreNota;
    private String descrizione;
    private String utenteCreazione;
    private LocalDateTime dataInserimento;
    private LocalDateTime dataUltimaModifica;
    private String utenteUltimaModifica;
    private String categoria;
    private String tags;
    private Boolean pubblica;
    private Integer priorita;
    private String stato;
    
    // Constructors
    public AnnotazioneResponse() {}
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getVersioneNota() {
        return versioneNota;
    }
    
    public void setVersioneNota(String versioneNota) {
        this.versioneNota = versioneNota;
    }
    
    public String getValoreNota() {
        return valoreNota;
    }
    
    public void setValoreNota(String valoreNota) {
        this.valoreNota = valoreNota;
    }
    
    public String getDescrizione() {
        return descrizione;
    }
    
    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }
    
    public String getUtenteCreazione() {
        return utenteCreazione;
    }
    
    public void setUtenteCreazione(String utenteCreazione) {
        this.utenteCreazione = utenteCreazione;
    }
    
    public LocalDateTime getDataInserimento() {
        return dataInserimento;
    }
    
    public void setDataInserimento(LocalDateTime dataInserimento) {
        this.dataInserimento = dataInserimento;
    }
    
    public LocalDateTime getDataUltimaModifica() {
        return dataUltimaModifica;
    }
    
    public void setDataUltimaModifica(LocalDateTime dataUltimaModifica) {
        this.dataUltimaModifica = dataUltimaModifica;
    }
    
    public String getUtenteUltimaModifica() {
        return utenteUltimaModifica;
    }
    
    public void setUtenteUltimaModifica(String utenteUltimaModifica) {
        this.utenteUltimaModifica = utenteUltimaModifica;
    }
    
    public String getCategoria() {
        return categoria;
    }
    
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }
    
    public Boolean getPubblica() {
        return pubblica;
    }
    
    public void setPubblica(Boolean pubblica) {
        this.pubblica = pubblica;
    }
    
    public Integer getPriorita() {
        return priorita;
    }
    
    public void setPriorita(Integer priorita) {
        this.priorita = priorita;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }
}
