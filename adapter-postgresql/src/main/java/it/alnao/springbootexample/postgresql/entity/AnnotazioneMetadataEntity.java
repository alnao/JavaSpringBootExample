package it.alnao.springbootexample.postgresql.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "nome_tabella_annotazione_metadata") //see CustomPhysicalNamingStrategy
public class AnnotazioneMetadataEntity {

    @Id
    private String id;

    /*
    @jakarta.persistence.Version
    @Column(name = "versione_jpa")
    private Long versionJPA;

    Aggiungi un campo version (integer/long) nella tua entity Annotazione.
    Ogni update verifica che la versione nel DB sia uguale a quella letta dall’utente.
    Se la versione è cambiata, l’update fallisce (Spring Data JPA supporta @Version).
    In caso di concorrenza, Spring solleva OptimisticLockException

    Spring/JPA gestisce automaticamente il valore e l’incremento ad ogni update.
    Quando crei una nuova annotazione, puoi lasciarlo null o a 0 (JPA lo inizializza).

    Logicamanente non può funzionare perchè nel AnnotazioneServiceImpl nel aggiornaAnnotazione
    viene ricaricata la AnnotazioneMetadata da DB e quindi la versione JPA viene sovrascritta.
    */

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

    @Column(name = "descrizione", columnDefinition = "TEXT")
    private String descrizione;

    @Column(name = "categoria")
    private String categoria;

    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;

    @Column(name = "pubblica")
    private Boolean pubblica;

    @Column(name = "priorita")
    private Integer priorita;

    @Column(name = "stato")
    private String stato;

    public AnnotazioneMetadataEntity() {
    }

    public String getStato() {
        return stato;
    }
    
    public void setStato(String stato) {
        this.stato = stato;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
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
/*
    public Long getVersionJPA() {
        return versionJPA;
    }

    public void setVersionJPA(Long versionJPA) {
        this.versionJPA = versionJPA;
    }
*/
}
