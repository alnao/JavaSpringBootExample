package it.alnao.springbootexample.api.mapper;

import it.alnao.springbootexample.api.dto.AnnotazioneResponse;
import it.alnao.springbootexample.api.dto.TransizioneStatoResponse;
import it.alnao.springbootexample.core.domain.Annotazione;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import it.alnao.springbootexample.core.domain.TransizioneStato;
import it.alnao.springbootexample.core.domain.auth.UserRole;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Copre i rami "null" dei mapper, non raggiunti dai test sul percorso felice.
 */
class MapperNullBranchTest {

    // ---------- AnnotazioneMapper ----------

    @Test
    void toResponse_withNull_returnsNull() {
        assertNull(AnnotazioneMapper.toResponse(null));
    }

    @Test
    void toResponse_withoutAnnotazione_mapsOnlyTheMetadata() {
        AnnotazioneMetadata meta = new AnnotazioneMetadata(UUID.randomUUID(), "1.0", "mario", "descr");
        AnnotazioneResponse response = AnnotazioneMapper.toResponse(new AnnotazioneCompleta(null, meta));
        assertNotNull(response);
        assertNull(response.getId());
        assertNull(response.getValoreNota());
        assertEquals("descr", response.getDescrizione());
        assertEquals("mario", response.getUtenteCreazione());
    }

    @Test
    void toResponse_withoutMetadata_mapsOnlyTheAnnotazione() {
        UUID id = UUID.randomUUID();
        AnnotazioneResponse response = AnnotazioneMapper.toResponse(
                new AnnotazioneCompleta(new Annotazione(id, "1.0", "testo"), null));
        assertNotNull(response);
        assertEquals(id, response.getId());
        assertEquals("testo", response.getValoreNota());
        assertNull(response.getDescrizione());
    }

    @Test
    void toResponse_withNeitherPart_returnsAnEmptyResponse() {
        AnnotazioneResponse response = AnnotazioneMapper.toResponse(new AnnotazioneCompleta(null, null));
        assertNotNull(response);
        assertNull(response.getId());
        assertNull(response.getDescrizione());
    }

    // ---------- TransizioneStatoMapper ----------

    @Test
    void transizione_toResponseWithNull_returnsNull() {
        assertNull(TransizioneStatoMapper.toResponse(null));
    }

    @Test
    void transizione_toResponseListWithNull_returnsNull() {
        assertNull(TransizioneStatoMapper.toResponseList(null));
    }

    @Test
    void transizione_toResponseListWithEmptyList_returnsEmptyList() {
        assertTrue(TransizioneStatoMapper.toResponseList(List.of()).isEmpty());
    }

    @Test
    void transizione_toResponseMapsEveryField() {
        TransizioneStatoResponse response = TransizioneStatoMapper.toResponse(new TransizioneStato(
                StatoAnnotazione.INSERITA, StatoAnnotazione.CONFERMATA, UserRole.ADMIN, "solo admin"));
        assertEquals("INSERITA", response.getStatoPartenza());
        assertEquals("CONFERMATA", response.getStatoArrivo());
        assertEquals("ADMIN", response.getRuoloRichiesto());
        assertEquals("solo admin", response.getDescrizione());
    }
}
