package it.alnao.springbootexample.sqlite.entity;

import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AnnotazioneMetadataSQLiteEntityTest {

    private AnnotazioneMetadata domain(UUID id) {
        AnnotazioneMetadata m = new AnnotazioneMetadata(id, "1.0", "mario", "una descrizione");
        m.setDataInserimento(LocalDateTime.of(2026, 1, 1, 10, 0));
        m.setDataUltimaModifica(LocalDateTime.of(2026, 1, 2, 11, 0));
        m.setUtenteUltimaModifica("luigi");
        m.setCategoria("lavoro");
        m.setTags("urgente,nota");
        m.setPubblica(true);
        m.setPriorita(4);
        m.setStato(StatoAnnotazione.DAINVIARE.getValue());
        return m;
    }

    @Test
    void constructorFromDomain_copiesEveryField() {
        UUID id = UUID.randomUUID();
        AnnotazioneMetadataSQLiteEntity entity = new AnnotazioneMetadataSQLiteEntity(domain(id));
        assertEquals(id.toString(), entity.getId());
        assertEquals("1.0", entity.getVersioneNota());
        assertEquals("mario", entity.getUtenteCreazione());
        assertEquals("luigi", entity.getUtenteUltimaModifica());
        assertEquals("una descrizione", entity.getDescrizione());
        assertEquals("lavoro", entity.getCategoria());
        assertEquals("urgente,nota", entity.getTags());
        assertTrue(entity.getPubblica());
        assertEquals(4, entity.getPriorita());
        assertEquals(StatoAnnotazione.DAINVIARE.getValue(), entity.getStato());
    }

    @Test
    void toDomain_roundTripsEveryField() {
        UUID id = UUID.randomUUID();
        AnnotazioneMetadata source = domain(id);
        AnnotazioneMetadata result = new AnnotazioneMetadataSQLiteEntity(source).toDomain();
        assertEquals(id, result.getId());
        assertEquals(source.getVersioneNota(), result.getVersioneNota());
        assertEquals(source.getUtenteCreazione(), result.getUtenteCreazione());
        assertEquals(source.getDataInserimento(), result.getDataInserimento());
        assertEquals(source.getDataUltimaModifica(), result.getDataUltimaModifica());
        assertEquals(source.getUtenteUltimaModifica(), result.getUtenteUltimaModifica());
        assertEquals(source.getDescrizione(), result.getDescrizione());
        assertEquals(source.getCategoria(), result.getCategoria());
        assertEquals(source.getTags(), result.getTags());
        assertEquals(source.getPubblica(), result.getPubblica());
        assertEquals(source.getPriorita(), result.getPriorita());
        assertEquals(source.getStato(), result.getStato());
    }

    @Test
    void defaultConstructor_appliesColumnDefaults() {
        AnnotazioneMetadataSQLiteEntity entity = new AnnotazioneMetadataSQLiteEntity();
        assertFalse(entity.getPubblica());
        assertEquals(1, entity.getPriorita());
        assertEquals("ERROR", entity.getStato());
        assertNull(entity.getId());
    }

    @Test
    void settersAndGetters_work() {
        AnnotazioneMetadataSQLiteEntity entity = new AnnotazioneMetadataSQLiteEntity();
        LocalDateTime inserimento = LocalDateTime.of(2026, 5, 5, 8, 0);
        LocalDateTime modifica = LocalDateTime.of(2026, 5, 6, 9, 0);
        entity.setId("abc");
        entity.setVersioneNota("2.0");
        entity.setUtenteCreazione("peach");
        entity.setDataInserimento(inserimento);
        entity.setDataUltimaModifica(modifica);
        entity.setUtenteUltimaModifica("toad");
        entity.setDescrizione("descr");
        entity.setCategoria("cat");
        entity.setTags("t1,t2");
        entity.setPubblica(true);
        entity.setPriorita(9);
        entity.setStato("INSERITA");

        assertEquals("abc", entity.getId());
        assertEquals("2.0", entity.getVersioneNota());
        assertEquals("peach", entity.getUtenteCreazione());
        assertEquals(inserimento, entity.getDataInserimento());
        assertEquals(modifica, entity.getDataUltimaModifica());
        assertEquals("toad", entity.getUtenteUltimaModifica());
        assertEquals("descr", entity.getDescrizione());
        assertEquals("cat", entity.getCategoria());
        assertEquals("t1,t2", entity.getTags());
        assertTrue(entity.getPubblica());
        assertEquals(9, entity.getPriorita());
        assertEquals("INSERITA", entity.getStato());
    }

    @Test
    void onCreate_fillsMissingDatesOnly() {
        AnnotazioneMetadataSQLiteEntity entity = new AnnotazioneMetadataSQLiteEntity();
        entity.onCreate();
        assertNotNull(entity.getDataInserimento());
        assertNotNull(entity.getDataUltimaModifica());
    }

    @Test
    void onCreate_keepsAnExistingDataInserimento() {
        AnnotazioneMetadataSQLiteEntity entity = new AnnotazioneMetadataSQLiteEntity();
        LocalDateTime originale = LocalDateTime.of(2020, 1, 1, 0, 0);
        entity.setDataInserimento(originale);
        entity.onCreate();
        assertEquals(originale, entity.getDataInserimento());
        assertNotNull(entity.getDataUltimaModifica());
    }

    @Test
    void onUpdate_refreshesDataUltimaModifica() {
        AnnotazioneMetadataSQLiteEntity entity = new AnnotazioneMetadataSQLiteEntity();
        entity.setDataUltimaModifica(LocalDateTime.of(2020, 1, 1, 0, 0));
        entity.onUpdate();
        assertTrue(entity.getDataUltimaModifica().isAfter(LocalDateTime.of(2020, 1, 1, 0, 0)));
    }
}
