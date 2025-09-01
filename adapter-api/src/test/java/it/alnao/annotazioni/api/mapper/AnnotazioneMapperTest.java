package it.alnao.annotazioni.api.mapper;

import it.alnao.annotazioni.api.dto.AnnotazioneResponse;
import it.alnao.annotazioni.port.domain.Annotazione;
import it.alnao.annotazioni.port.domain.AnnotazioneCompleta;
import it.alnao.annotazioni.port.domain.AnnotazioneMetadata;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AnnotazioneMapperTest {
    @Test
    void toResponse_shouldMapFields() {
        Annotazione a = new Annotazione();
        a.setId(UUID.randomUUID());
        a.setValoreNota("nota");
        a.setVersioneNota("v1");
        AnnotazioneMetadata m = new AnnotazioneMetadata();
        m.setDescrizione("desc");
        m.setUtenteCreazione("utente");
        AnnotazioneCompleta ac = new AnnotazioneCompleta(a, m);
        AnnotazioneResponse resp = AnnotazioneMapper.toResponse(ac);
        assertEquals(a.getId(), resp.getId());
        assertEquals("nota", resp.getValoreNota());
        assertEquals("desc", resp.getDescrizione());
        assertEquals("utente", resp.getUtenteCreazione());
    }

    @Test
    void toResponseList_shouldReturnEmptyListForNull() {
        assertTrue(AnnotazioneMapper.toResponseList(null).isEmpty());
    }

    @Test
    void toResponseList_shouldMapList() {
        Annotazione a = new Annotazione();
        a.setId(UUID.randomUUID());
        AnnotazioneMetadata m = new AnnotazioneMetadata();
        AnnotazioneCompleta ac = new AnnotazioneCompleta(a, m);
        assertEquals(1, AnnotazioneMapper.toResponseList(Collections.singletonList(ac)).size());
    }
}
