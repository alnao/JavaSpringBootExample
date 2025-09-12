package it.alnao.springbootexample.core.domain;

import java.time.LocalDateTime;

/**
 * Dominio per rappresentare un record di storico cambio stato annotazione
 */
public class AnnotazioneStoricoStati {
    
    private String idOperazione;
    private String idAnnotazione;
    private String versione;
    private String statoNew;
    private String statoOld;
    private String utente;
    private LocalDateTime dataModifica;
    private String notaOperazione;

    // Costruttori
    public AnnotazioneStoricoStati() {}

    public AnnotazioneStoricoStati(String idOperazione, String idAnnotazione, String versione, 
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
        return "AnnotazioneStoricoStati{" +
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
