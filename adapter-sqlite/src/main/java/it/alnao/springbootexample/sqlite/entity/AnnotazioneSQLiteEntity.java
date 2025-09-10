package it.alnao.springbootexample.sqlite.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "annotazioni")
public class AnnotazioneSQLiteEntity {
    @Id
    //@UuidGenerator
    private String id;
    @Column(name = "valore_nota", columnDefinition = "TEXT")
    private String valoreNota;
    @Column(name = "versione_nota")
    private String versioneNota;
    @Column(name = "data_inserimento")
    private LocalDateTime dataInserimento;
    @Column(name = "data_ultima_modifica")
    private LocalDateTime dataUltimaModifica;

    public AnnotazioneSQLiteEntity() {}
    public AnnotazioneSQLiteEntity(UUID id, String valoreNota, String versioneNota) {
        this.id = id.toString();
        this.valoreNota = valoreNota;
        this.versioneNota = versioneNota;
        this.dataInserimento = LocalDateTime.now();
        this.dataUltimaModifica = LocalDateTime.now();
    }
    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getValoreNota() { return valoreNota; }
    public void setValoreNota(String valoreNota) { this.valoreNota = valoreNota; }
    public String getVersioneNota() { return versioneNota; }
    public void setVersioneNota(String versioneNota) { this.versioneNota = versioneNota; }
    public LocalDateTime getDataInserimento() { return dataInserimento; }
    public void setDataInserimento(LocalDateTime dataInserimento) { this.dataInserimento = dataInserimento; }
    public LocalDateTime getDataUltimaModifica() { return dataUltimaModifica; }
    public void setDataUltimaModifica(LocalDateTime dataUltimaModifica) { this.dataUltimaModifica = dataUltimaModifica; }
}
