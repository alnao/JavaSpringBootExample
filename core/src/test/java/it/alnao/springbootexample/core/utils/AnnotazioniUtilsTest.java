package it.alnao.springbootexample.core.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnnotazioniUtilsTest {

    @Test
    void incrementaVersione_null_ritorna10() {
        assertEquals("1.0", AnnotazioniUtils.incrementaVersione(null));
    }

    @Test
    void incrementaVersione_vuota_ritorna10() {
        assertEquals("1.0", AnnotazioniUtils.incrementaVersione(""));
    }

    @Test
    void incrementaVersione_standard_incrementaMinore() {
        assertEquals("1.1", AnnotazioniUtils.incrementaVersione("1.0"));
        assertEquals("2.4", AnnotazioniUtils.incrementaVersione("2.3"));
        assertEquals("0.10", AnnotazioniUtils.incrementaVersione("0.9"));
    }

    @Test
    void incrementaVersione_conPrefissoV_mantienePrefisso() {
        assertEquals("v1.1", AnnotazioniUtils.incrementaVersione("v1.0"));
        assertEquals("v2.4", AnnotazioniUtils.incrementaVersione("v2.3"));
    }

    @Test
    void incrementaVersione_formatoNonStandard_aggiunge1() {
        assertEquals("alpha.1", AnnotazioniUtils.incrementaVersione("alpha"));
        assertEquals("release-1.1", AnnotazioniUtils.incrementaVersione("release-1"));
    }
}
