package it.alnao.springbootexample.azure.entity;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import org.springframework.data.annotation.Id;
import java.time.LocalDateTime;

@Container(containerName = "annotazione_storico_stati")
public class AnnotazioneStoricoStatiCosmosEntity {
    @Id
    private String id;
    @PartitionKey
    private String idAnnotazione;
    private String versione;
    private String statoNew;
    private String statoOld;
    private String utente;
    private String notaOperazione;
    private LocalDateTime dataCambio;

    // Getters e setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getIdAnnotazione() { return idAnnotazione; }
    public void setIdAnnotazione(String idAnnotazione) { this.idAnnotazione = idAnnotazione; }
    public String getVersione() { return versione; }
    public void setVersione(String versione) { this.versione = versione; }
    public String getStatoNew() { return statoNew; }
    public void setStatoNew(String statoNew) { this.statoNew = statoNew; }
    public String getStatoOld() { return statoOld; }
    public void setStatoOld(String statoOld) { this.statoOld = statoOld; }
    public String getUtente() { return utente; }
    public void setUtente(String utente) { this.utente = utente; }
    public String getNotaOperazione() { return notaOperazione; }
    public void setNotaOperazione(String notaOperazione) { this.notaOperazione = notaOperazione; }
    public LocalDateTime getDataCambio() { return dataCambio; }
    public void setDataCambio(LocalDateTime dataCambio) { this.dataCambio = dataCambio; }
}
