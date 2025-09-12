package it.alnao.springbootexample.sqlite.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity JPA per lo storico dei cambi di stato delle annotazioni su SQLite
 */
@Entity
@Table(name = "annotazioni_storicoStati")
public class AnnotazioneStoricoStatiSQLiteEntity {
    
    @Id
    @Column(name = "id_operazione")
    private String idOperazione;
    
    @Column(name = "id_annotazione", nullable = false)
    private String idAnnotazione;
    
    @Column(name = "versione")
    private String versione;
    
    @Column(name = "stato_new", nullable = false)
    private String statoNew;
    
    @Column(name = "stato_old")
    private String statoOld;
    
    @Column(name = "utente", nullable = false)
    private String utente;
    
    @Column(name = "data_modifica", nullable = false)
    private LocalDateTime dataModifica;
    
    @Column(name = "nota_operazione", columnDefinition = "TEXT")
    private String notaOperazione;

    // Costruttori
    public AnnotazioneStoricoStatiSQLiteEntity() {}

    public AnnotazioneStoricoStatiSQLiteEntity(String idOperazione, String idAnnotazione, String versione, 
                                              String statoNew, String statoOld, String utente, 
                                              LocalDateTime dataModifica, String notaOperazione) {
        this.idOperazione = idOperazione;
        this.idAnnotazione = idAnnotazione;
        this.versione = versione;
        this.statoNew = statoNew;
        this.statoOld = statoOld;
        this.utente = utente;
        this.dataModifica = dataModifica;
        this.notaOperazione = notaOperazione;
    }

    // Getters e Setters
    public String getIdOperazione() {
        return idOperazione;
    }

    public void setIdOperazione(String idOperazione) {
        this.idOperazione = idOperazione;
    }

    public String getIdAnnotazione() {
        return idAnnotazione;
    }

    public void setIdAnnotazione(String idAnnotazione) {
        this.idAnnotazione = idAnnotazione;
    }

    public String getVersione() {
        return versione;
    }

    public void setVersione(String versione) {
        this.versione = versione;
    }

    public String getStatoNew() {
        return statoNew;
    }

    public void setStatoNew(String statoNew) {
        this.statoNew = statoNew;
    }

    public String getStatoOld() {
        return statoOld;
    }

    public void setStatoOld(String statoOld) {
        this.statoOld = statoOld;
    }

    public String getUtente() {
        return utente;
    }

    public void setUtente(String utente) {
        this.utente = utente;
    }

    public LocalDateTime getDataModifica() {
        return dataModifica;
    }

    public void setDataModifica(LocalDateTime dataModifica) {
        this.dataModifica = dataModifica;
    }

    public String getNotaOperazione() {
        return notaOperazione;
    }

    public void setNotaOperazione(String notaOperazione) {
        this.notaOperazione = notaOperazione;
    }

    @Override
    public String toString() {
        return "AnnotazioneStoricoStatiSQLiteEntity{" +
                "idOperazione='" + idOperazione + '\'' +
                ", idAnnotazione='" + idAnnotazione + '\'' +
                ", versione='" + versione + '\'' +
                ", statoNew='" + statoNew + '\'' +
                ", statoOld='" + statoOld + '\'' +
                ", utente='" + utente + '\'' +
                ", dataModifica=" + dataModifica +
                ", notaOperazione='" + notaOperazione + '\'' +
                '}';
    }
}
