package it.alnao.springbootexample.onprem.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "#{@noSqlTableConfig.getAnnotazioniTableName()}")
public class AnnotazioneEntity {
    
    @Id
    private String id;
    private String versioneNota;
    private String valoreNota;

    public AnnotazioneEntity() {
    }

    public AnnotazioneEntity(String id, String versioneNota, String valoreNota) {
        this.id = id;
        this.versioneNota = versioneNota;
        this.valoreNota = valoreNota;
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

    public String getValoreNota() {
        return valoreNota;
    }

    public void setValoreNota(String valoreNota) {
        this.valoreNota = valoreNota;
    }
}
