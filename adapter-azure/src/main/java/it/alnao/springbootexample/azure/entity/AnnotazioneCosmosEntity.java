package it.alnao.springbootexample.azure.entity;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import org.springframework.data.annotation.Id;

@Container(containerName = "annotazioni")
public class AnnotazioneCosmosEntity {
    @Id
    @PartitionKey
    private String id;
    private String versioneNota;
    private String valoreNota;

    public AnnotazioneCosmosEntity() {}
    public AnnotazioneCosmosEntity(String id, String versioneNota, String valoreNota) {
        this.id = id;
        this.versioneNota = versioneNota;
        this.valoreNota = valoreNota;
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getVersioneNota() { return versioneNota; }
    public void setVersioneNota(String versioneNota) { this.versioneNota = versioneNota; }
    public String getValoreNota() { return valoreNota; }
    public void setValoreNota(String valoreNota) { this.valoreNota = valoreNota; }
}
