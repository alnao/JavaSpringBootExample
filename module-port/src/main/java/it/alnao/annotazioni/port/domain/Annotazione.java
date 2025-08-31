package it.alnao.annotazioni.port.domain;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entità dominio per l'annotazione (parte NoSQL)
 */
public class Annotazione {
    
    private UUID id;
    private String versioneNota;
    private String valoreNota;
    
    public Annotazione() {
        this.id = UUID.randomUUID();
        this.versioneNota = "1.0";
    }
    
    public Annotazione(String valoreNota) {
        this();
        this.valoreNota = valoreNota;
    }
    
    public Annotazione(UUID id, String versioneNota, String valoreNota) {
        this.id = id;
        this.versioneNota = versioneNota;
        this.valoreNota = valoreNota;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getVersioneNota() {
        return versioneNota;
    }
    
    public void setVersioneNota(String versioneNota) {
        this.versioneNota = versioneNota;
    }
    
    public String getValoreNota() {
        return valoreNota;
    }
    
    public void setValoreNota(String valoreNota) {
        this.valoreNota = valoreNota;
    }
    
    @Override
    public String toString() {
        return "Annotazione{" +
                "id=" + id +
                ", versioneNota=" + versioneNota +
                ", valoreNota='" + valoreNota + '\'' +
                '}';
    }
}
