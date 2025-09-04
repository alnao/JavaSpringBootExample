package it.alnao.springbootexample.aws.entity;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import java.time.LocalDateTime;

@DynamoDbBean
public class AnnotazioneStoricoDynamoEntity {
    private String id;
    private String idOriginale;
    private String versioneNota;
    private String valoreNota;
    private String descrizione;
    private String utente;
    private String categoria;
    private String tags;
    private Boolean pubblica;
    private Integer priorita;
    private LocalDateTime dataModifica;

    @DynamoDbPartitionKey
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getIdOriginale() { return idOriginale; }
    public void setIdOriginale(String idOriginale) { this.idOriginale = idOriginale; }
    public String getVersioneNota() { return versioneNota; }
    public void setVersioneNota(String versioneNota) { this.versioneNota = versioneNota; }
    public String getValoreNota() { return valoreNota; }
    public void setValoreNota(String valoreNota) { this.valoreNota = valoreNota; }
    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }
    public String getUtente() { return utente; }
    public void setUtente(String utente) { this.utente = utente; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public Boolean getPubblica() { return pubblica; }
    public void setPubblica(Boolean pubblica) { this.pubblica = pubblica; }
    public Integer getPriorita() { return priorita; }
    public void setPriorita(Integer priorita) { this.priorita = priorita; }
    public LocalDateTime getDataModifica() { return dataModifica; }
    public void setDataModifica(LocalDateTime dataModifica) { this.dataModifica = dataModifica; }
}
