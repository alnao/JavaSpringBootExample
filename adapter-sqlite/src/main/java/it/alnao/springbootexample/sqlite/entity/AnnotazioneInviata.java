package it.alnao.springbootexample.sqlite.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "annotazioni_inviate")
public class AnnotazioneInviata {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "annotazione_id", nullable = false)
    private UUID annotazioneId;
    
    @Column(name = "contenuto", columnDefinition = "TEXT")
    private String contenuto;
    
    @Column(name = "data_invio", nullable = false)
    private LocalDateTime dataInvio;
    
    @Column(name = "stato_invio", nullable = false)
    private String statoInvio;
    
    @Column(name = "messaggio_errore", columnDefinition = "TEXT")
    private String messaggioErrore;
    
    // costruttori
    public AnnotazioneInviata() {}
    
    public AnnotazioneInviata(UUID annotazioneId, String contenuto, LocalDateTime dataInvio, String statoInvio) {
        this.annotazioneId = annotazioneId;
        this.contenuto = contenuto;
        this.dataInvio = dataInvio;
        this.statoInvio = statoInvio;
    }
    
    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public UUID getAnnotazioneId() { return annotazioneId; }
    public void setAnnotazioneId(UUID annotazioneId) { this.annotazioneId = annotazioneId; }
    
    public String getContenuto() { return contenuto; }
    public void setContenuto(String contenuto) { this.contenuto = contenuto; }
    
    public LocalDateTime getDataInvio() { return dataInvio; }
    public void setDataInvio(LocalDateTime dataInvio) { this.dataInvio = dataInvio; }
    
    public String getStatoInvio() { return statoInvio; }
    public void setStatoInvio(String statoInvio) { this.statoInvio = statoInvio; }
    
    public String getMessaggioErrore() { return messaggioErrore; }
    public void setMessaggioErrore(String messaggioErrore) { this.messaggioErrore = messaggioErrore; }
    
    @Override
    public String toString() {
        return "AnnotazioneInviata{" +
                "id=" + id +
                ", annotazioneId=" + annotazioneId +
                ", contenuto='" + contenuto + '\'' +
                ", dataInvio=" + dataInvio +
                ", statoInvio='" + statoInvio + '\'' +
                ", messaggioErrore='" + messaggioErrore + '\'' +
                '}';
    }
}
