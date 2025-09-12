package it.alnao.springbootexample.onprem.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * Entity per lo storico dei cambi di stato delle annotazioni in MongoDB
 */
@Document(collection = "annotazioni_storicoStati")
public class AnnotazioneStoricoStatiEntity {

    @Id
    private String idOperazione;

    @Field("idAnnotazione")
    private String idAnnotazione;

    @Field("versione")
    private String versione;

    @Field("statoNew")
    private String statoNew;

    @Field("statoOld")
    private String statoOld;

    @Field("utente")
    private String utente;

    @Field("dataModifica")
    private LocalDateTime dataModifica;

    @Field("notaOperazione")
    private String notaOperazione;

    // Costruttori
    public AnnotazioneStoricoStatiEntity() {}

    public AnnotazioneStoricoStatiEntity(String idOperazione, String idAnnotazione, String versione, 
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
        return "AnnotazioneStoricoStatiEntity{" +
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
