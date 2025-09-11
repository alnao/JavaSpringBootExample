package it.alnao.springbootexample.app.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import it.alnao.springbootexample.core.config.NoSqlTableConfig;
import it.alnao.springbootexample.core.service.AnnotazioneService;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public AnnotazioneService mockAnnotazioneService() {
        return Mockito.mock(AnnotazioneService.class);
    }

    @Bean
    @Primary
    public NoSqlTableConfig testNoSqlTableConfig() {
        NoSqlTableConfig config = new NoSqlTableConfig();
        config.setAnnotazioniTableName("test_annotazioni");
        config.setAnnotazioniStoricoTableName("test_annotazioni_storico");
        return config;
    }
}
