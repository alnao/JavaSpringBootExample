package it.alnao.springbootexample.port.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "gestione-personale.nosql")
public class NoSqlTableConfig {

    private String annotazioniTableName = ""; //annotazioni
    private String annotazioniStoricoTableName = ""; //annotazioni_storico

    public String getAnnotazioniTableName() {
        return annotazioniTableName;
    }

    public void setAnnotazioniTableName(String annotazioniTableName) {
        this.annotazioniTableName = annotazioniTableName;
    }

    public String getAnnotazioniStoricoTableName() {
        return annotazioniStoricoTableName;
    }

    public void setAnnotazioniStoricoTableName(String annotazioniStoricoTableName) {
        this.annotazioniStoricoTableName = annotazioniStoricoTableName;
    }
}
