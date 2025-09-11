package it.alnao.springbootexample.core.domain;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entità dominio per i metadati dell'annotazione (parte SQL)
 */
public class AnnotazioneMetadata {
    
    private UUID id;
    private String versioneNota;
    private String utenteCreazione;
    private LocalDateTime dataInserimento;
    private LocalDateTime dataUltimaModifica;
    private String utenteUltimaModifica;
    private String descrizione;
    private String categoria;
    private String tags;
    private Boolean pubblica;
    private Integer priorita;
    
    public AnnotazioneMetadata() {
        this.dataInserimento = LocalDateTime.now();
        this.dataUltimaModifica = LocalDateTime.now();
        this.pubblica = false;
        this.priorita = 1;
    }
    
    public AnnotazioneMetadata(UUID id, String versioneNota, String utenteCreazione, String descrizione) {
        this();
        this.id = id;
        this.versioneNota = versioneNota;
        this.utenteCreazione = utenteCreazione;
        this.utenteUltimaModifica = utenteCreazione;
        this.descrizione = descrizione;
    }
    
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
    
    public String getDescrizione() {
        return descrizione;
    }
    
    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
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
    
    public void aggiorna(String utenteModifica) {
        this.dataUltimaModifica = LocalDateTime.now();
        this.utenteUltimaModifica = utenteModifica;
    }
    
    @Override
    public String toString() {
    return "AnnotazioneMetadata{" +
        "id=" + id +
        ", versioneNota='" + versioneNota + '\'' +
        ", utenteCreazione='" + utenteCreazione + '\'' +
        ", dataInserimento=" + dataInserimento +
        ", dataUltimaModifica=" + dataUltimaModifica +
        ", utenteUltimaModifica='" + utenteUltimaModifica + '\'' +
        ", descrizione='" + descrizione + '\'' +
        ", categoria='" + categoria + '\'' +
        ", tags='" + tags + '\'' +
        ", pubblica=" + pubblica +
        ", priorita=" + priorita +
        '}';
    }
}
