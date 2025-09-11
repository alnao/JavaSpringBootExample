package it.alnao.springbootexample.sqlite.entity;

import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity JPA per AnnotazioneMetadata su SQLite
 */
@Entity
@Table(name = "annotazioni_metadata")
public class AnnotazioneMetadataSQLiteEntity {
    
    @Id
    //@UuidGenerator
    private String id;
    
    @Column(name = "versione_nota")
    private String versioneNota;
    
    @Column(name = "utente_creazione")
    private String utenteCreazione;
    
    @Column(name = "data_inserimento")
    private LocalDateTime dataInserimento;
    
    @Column(name = "data_ultima_modifica")
    private LocalDateTime dataUltimaModifica;
    
    @Column(name = "utente_ultima_modifica")
    private String utenteUltimaModifica;
    
    @Column(columnDefinition = "TEXT")
    private String descrizione;
    
    private String categoria;
    
    @Column(columnDefinition = "TEXT")
    private String tags;
    
    private Boolean pubblica = false;
    
    private Integer priorita = 1;
    
    @PrePersist
    protected void onCreate() {
        if (dataInserimento == null) {
            dataInserimento = LocalDateTime.now();
        }
        dataUltimaModifica = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        dataUltimaModifica = LocalDateTime.now();
    }
    
    // Costruttori
    public AnnotazioneMetadataSQLiteEntity() {}
    
    public AnnotazioneMetadataSQLiteEntity(AnnotazioneMetadata metadata) {
    this.id = metadata.getId().toString();
        this.versioneNota = metadata.getVersioneNota();
        this.utenteCreazione = metadata.getUtenteCreazione();
        this.dataInserimento = metadata.getDataInserimento();
        this.dataUltimaModifica = metadata.getDataUltimaModifica();
        this.utenteUltimaModifica = metadata.getUtenteUltimaModifica();
        this.descrizione = metadata.getDescrizione();
        this.categoria = metadata.getCategoria();
        this.tags = metadata.getTags();
        this.pubblica = metadata.getPubblica();
        this.priorita = metadata.getPriorita();
    }
    
    public AnnotazioneMetadata toDomain() {
        AnnotazioneMetadata metadata = new AnnotazioneMetadata();
    metadata.setId(UUID.fromString(this.id));
        metadata.setVersioneNota(this.versioneNota);
        metadata.setUtenteCreazione(this.utenteCreazione);
        metadata.setDataInserimento(this.dataInserimento);
        metadata.setDataUltimaModifica(this.dataUltimaModifica);
        metadata.setUtenteUltimaModifica(this.utenteUltimaModifica);
        metadata.setDescrizione(this.descrizione);
        metadata.setCategoria(this.categoria);
        metadata.setTags(this.tags);
        metadata.setPubblica(this.pubblica);
        metadata.setPriorita(this.priorita);
        return metadata;
    }
    
    // Getters e Setters
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
}
