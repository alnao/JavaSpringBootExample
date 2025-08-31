package it.alnao.annotazioni.aws.entity;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class AnnotazioneDynamoEntity {
    
    private String id;
    private String versioneNota;
    private String valoreNota;

    public AnnotazioneDynamoEntity() {
    }

    public AnnotazioneDynamoEntity(String id, String versioneNota, String valoreNota) {
        this.id = id;
        this.versioneNota = versioneNota;
        this.valoreNota = valoreNota;
    }

    @DynamoDbPartitionKey
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
