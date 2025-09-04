package it.alnao.springbootexample.port.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "gestione-personale.sql")
public class SqlTableConfig {

    private String annotazioniTableName = "annotazioni";
    private String annotazioniMetadataTableName = "annotazioni_metadata";

    public String getAnnotazioniTableName() {
        return annotazioniTableName;
    }

    public void setAnnotazioniTableName(String annotazioniTableName) {
        this.annotazioniTableName = annotazioniTableName;
    }

    public String getAnnotazioniMetadataTableName() {
        return annotazioniMetadataTableName;
    }

    public void setAnnotazioniMetadataTableName(String annotazioniMetadataTableName) {
        this.annotazioniMetadataTableName = annotazioniMetadataTableName;
    }
}
