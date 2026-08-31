package it.alnao.springbootexample.core.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NoSqlTableConfigTest {

    @Test
    void defaults_areEmptyStrings() {
        NoSqlTableConfig config = new NoSqlTableConfig();
        assertEquals("", config.getAnnotazioniTableName());
        assertEquals("", config.getAnnotazioniStoricoTableName());
    }

    @Test
    void settersAndGettersWork() {
        NoSqlTableConfig config = new NoSqlTableConfig();
        config.setAnnotazioniTableName("annotazioni");
        config.setAnnotazioniStoricoTableName("annotazioni_storico");
        assertEquals("annotazioni", config.getAnnotazioniTableName());
        assertEquals("annotazioni_storico", config.getAnnotazioniStoricoTableName());
    }
}
