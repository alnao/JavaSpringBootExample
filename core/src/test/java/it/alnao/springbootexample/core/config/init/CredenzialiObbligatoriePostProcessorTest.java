package it.alnao.springbootexample.core.config.init;

import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.*;

class CredenzialiObbligatoriePostProcessorTest {

    private final CredenzialiObbligatoriePostProcessor postProcessor = new CredenzialiObbligatoriePostProcessor();

    @Test
    void senzaLista_nessunControllo() {
        MockEnvironment environment = new MockEnvironment();
        assertDoesNotThrow(() -> postProcessor.postProcessEnvironment(environment, null));
    }

    @Test
    void variabiliPresenti_nessunErrore() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty(CredenzialiObbligatoriePostProcessor.PROPRIETA, "POSTGRES_PASSWORD, MONGODB_URI")
                .withProperty("POSTGRES_PASSWORD", "segreta")
                .withProperty("MONGODB_URI", "mongodb://u:p@mongo:27017/db");
        assertDoesNotThrow(() -> postProcessor.postProcessEnvironment(environment, null));
    }

    @Test
    void variabileAssenteOVuota_erroreConINomi() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty(CredenzialiObbligatoriePostProcessor.PROPRIETA, "POSTGRES_PASSWORD,MONGODB_URI,JWT_SECRET")
                .withProperty("MONGODB_URI", "   ")
                .withProperty("JWT_SECRET", "valore-che-non-deve-comparire");
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> postProcessor.postProcessEnvironment(environment, null));
        assertEquals("Credenziale mancante: POSTGRES_PASSWORD, MONGODB_URI", e.getMessage());
        assertFalse(e.getMessage().contains("valore-che-non-deve-comparire"));
    }

    @Test
    void valoreDaProprietaDiConfigurazione_consideratoPresente() {
        // come le chiavi di primo livello di application-dev.yml, che non sono variabili d'ambiente
        MockEnvironment environment = new MockEnvironment()
                .withProperty(CredenzialiObbligatoriePostProcessor.PROPRIETA, "POSTGRES_PASSWORD")
                .withProperty("POSTGRES_PASSWORD", "gestione_annotazioni_pass");
        assertDoesNotThrow(() -> postProcessor.postProcessEnvironment(environment, null));
    }

    @Test
    void elementiVuotiNellaLista_ignorati() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty(CredenzialiObbligatoriePostProcessor.PROPRIETA, " , ");
        assertDoesNotThrow(() -> postProcessor.postProcessEnvironment(environment, null));
    }

    @Test
    void ordine_dopoIlCaricamentoDegliYml() {
        assertEquals(Ordered.LOWEST_PRECEDENCE, postProcessor.getOrder());
    }
}
