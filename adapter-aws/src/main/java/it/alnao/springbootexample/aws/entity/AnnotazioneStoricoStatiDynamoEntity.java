package it.alnao.springbootexample.aws.entity;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;

/**
 * Entity per lo storico dei cambi di stato delle annotazioni in DynamoDB
 */
@DynamoDbBean
public class AnnotazioneStoricoStatiDynamoEntity {

    private String idOperazione;
    private String idAnnotazione;
    private String versione;
    private String statoNew;
    private String statoOld;
    private String utente;
    private String dataModifica; // Uso String per semplicità con DynamoDB
    private String notaOperazione;

    // Costruttori
    public AnnotazioneStoricoStatiDynamoEntity() {}

    public AnnotazioneStoricoStatiDynamoEntity(String idOperazione, String idAnnotazione, String versione, 
                                              String statoNew, String statoOld, String utente, 
                                              String dataModifica, String notaOperazione) {
        this.idOperazione = idOperazione;
        this.idAnnotazione = idAnnotazione;
        this.versione = versione;
        this.statoNew = statoNew;
        this.statoOld = statoOld;
        this.utente = utente;
        this.dataModifica = dataModifica;
        this.notaOperazione = notaOperazione;
    }

    // Getters e Setters con annotazioni DynamoDB
    @DynamoDbPartitionKey
    public String getIdOperazione() {
        return idOperazione;
    }

    public void setIdOperazione(String idOperazione) {
        this.idOperazione = idOperazione;
    }

    @DynamoDbAttribute("idAnnotazione")
    public String getIdAnnotazione() {
        return idAnnotazione;
    }

    public void setIdAnnotazione(String idAnnotazione) {
        this.idAnnotazione = idAnnotazione;
    }

    @DynamoDbAttribute("versione")
    public String getVersione() {
        return versione;
    }

    public void setVersione(String versione) {
        this.versione = versione;
    }

    @DynamoDbAttribute("statoNew")
    public String getStatoNew() {
        return statoNew;
    }

    public void setStatoNew(String statoNew) {
        this.statoNew = statoNew;
    }

    @DynamoDbAttribute("statoOld")
    public String getStatoOld() {
        return statoOld;
    }

    public void setStatoOld(String statoOld) {
        this.statoOld = statoOld;
    }

    @DynamoDbAttribute("utente")
    public String getUtente() {
        return utente;
    }

    public void setUtente(String utente) {
        this.utente = utente;
    }

    @DynamoDbAttribute("dataModifica")
    public String getDataModifica() {
        return dataModifica;
    }

    public void setDataModifica(String dataModifica) {
        this.dataModifica = dataModifica;
    }

    @DynamoDbAttribute("notaOperazione")
    public String getNotaOperazione() {
        return notaOperazione;
    }

    public void setNotaOperazione(String notaOperazione) {
        this.notaOperazione = notaOperazione;
    }

    @Override
    public String toString() {
        return "AnnotazioneStoricoStatiDynamoEntity{" +
                "idOperazione='" + idOperazione + '\'' +
                ", idAnnotazione='" + idAnnotazione + '\'' +
                ", versione='" + versione + '\'' +
                ", statoNew='" + statoNew + '\'' +
                ", statoOld='" + statoOld + '\'' +
                ", utente='" + utente + '\'' +
                ", dataModifica='" + dataModifica + '\'' +
                ", notaOperazione='" + notaOperazione + '\'' +
                '}';
    }
}
