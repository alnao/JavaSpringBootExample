package it.alnao.springbootexample.azure.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "annotazione_metadata")
public class AnnotazioneMetadataSqlServerEntity {
    @Id
    private String id;
    private String versioneNota;
    private String utenteCreazione;
    private LocalDateTime dataInserimento;
    private LocalDateTime dataUltimaModifica;
    private String utenteUltimaModifica;
    @Column(columnDefinition = "TEXT")
    private String descrizione;
    private String categoria;
    @Column(columnDefinition = "TEXT")
    private String tags;
    private Boolean pubblica;
    private Integer priorita;
    private String stato;
    public AnnotazioneMetadataSqlServerEntity() {}
    // Getters e setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getVersioneNota() { return versioneNota; }
    public void setVersioneNota(String versioneNota) { this.versioneNota = versioneNota; }
    public String getUtenteCreazione() { return utenteCreazione; }
    public void setUtenteCreazione(String utenteCreazione) { this.utenteCreazione = utenteCreazione; }
    public LocalDateTime getDataInserimento() { return dataInserimento; }
    public void setDataInserimento(LocalDateTime dataInserimento) { this.dataInserimento = dataInserimento; }
    public LocalDateTime getDataUltimaModifica() { return dataUltimaModifica; }
    public void setDataUltimaModifica(LocalDateTime dataUltimaModifica) { this.dataUltimaModifica = dataUltimaModifica; }
    public String getUtenteUltimaModifica() { return utenteUltimaModifica; }
    public void setUtenteUltimaModifica(String utenteUltimaModifica) { this.utenteUltimaModifica = utenteUltimaModifica; }
    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public Boolean getPubblica() { return pubblica; }
    public void setPubblica(Boolean pubblica) { this.pubblica = pubblica; }
    public Integer getPriorita() { return priorita; }
    public void setPriorita(Integer priorita) { this.priorita = priorita; }
    public String getStato() { return stato; }
    public void setStato(String stato) { this.stato = stato; }
}
