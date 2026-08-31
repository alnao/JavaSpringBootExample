package it.alnao.springbootexample.sqlite.service;

import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.sqlite.entity.AnnotazioneSQLiteEntity;
import it.alnao.springbootexample.sqlite.entity.AnnotazioneMetadataSQLiteEntity;
import it.alnao.springbootexample.sqlite.repository.AnnotazioneSQLiteRepository;
import it.alnao.springbootexample.sqlite.repository.AnnotazioneMetadataSQLiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AnnotazioneServiceSQLiteImplTest {

    @Mock AnnotazioneSQLiteRepository annotazioneRepository;
    @Mock AnnotazioneMetadataSQLiteRepository metadataRepository;
    @InjectMocks AnnotazioneServiceSQLiteImpl service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void creaAnnotazione_savesAndReturnsResult() {
        UUID id = UUID.randomUUID();
        AnnotazioneSQLiteEntity entity = new AnnotazioneSQLiteEntity(id, "nota test", "1");
        when(annotazioneRepository.save(any())).thenReturn(entity);
        when(metadataRepository.save(any())).thenReturn(new AnnotazioneMetadataSQLiteEntity());

        AnnotazioneCompleta result = service.creaAnnotazione("nota test", "desc", "user");

        assertNotNull(result);
        assertEquals("nota test", result.getAnnotazione().getValoreNota());
        verify(annotazioneRepository).save(any());
        verify(metadataRepository).save(any());
    }

    @Test
    void aggiornaAnnotazione_whenFound_updatesAndReturns() {
        UUID id = UUID.randomUUID();
        AnnotazioneSQLiteEntity entity = new AnnotazioneSQLiteEntity(id, "vecchio", "1");
        AnnotazioneMetadataSQLiteEntity metaEntity = buildMetaEntity(id, "INSERITA");

        when(annotazioneRepository.findById(id.toString())).thenReturn(Optional.of(entity));
        when(metadataRepository.findById(id.toString())).thenReturn(Optional.of(metaEntity));
        when(annotazioneRepository.save(any())).thenReturn(entity);
        when(metadataRepository.save(any())).thenReturn(metaEntity);

        AnnotazioneCompleta result = service.aggiornaAnnotazione(id, "nuovo", "nuova desc", "user");

        assertNotNull(result);
        verify(annotazioneRepository).save(any());
        verify(metadataRepository).save(any());
    }

    @Test
    void aggiornaAnnotazione_whenMetadataMissing_returnsNull() {
        UUID id = UUID.randomUUID();
        AnnotazioneSQLiteEntity entity = new AnnotazioneSQLiteEntity(id, "vecchio", "1");
        when(annotazioneRepository.findById(id.toString())).thenReturn(Optional.of(entity));
        when(metadataRepository.findById(id.toString())).thenReturn(Optional.empty());
        when(annotazioneRepository.save(any())).thenReturn(entity);

        AnnotazioneCompleta result = service.aggiornaAnnotazione(id, "nuovo", "desc", "user");

        assertNull(result);
    }

    @Test
    void aggiornaAnnotazione_whenAnnotazioneNotFound_returnsNull() {
        UUID id = UUID.randomUUID();
        when(annotazioneRepository.findById(id.toString())).thenReturn(Optional.empty());

        AnnotazioneCompleta result = service.aggiornaAnnotazione(id, "nuovo", "desc", "user");

        assertNull(result);
    }

    @Test
    void trovaPerID_whenFound_returnsResult() {
        UUID id = UUID.randomUUID();
        AnnotazioneSQLiteEntity entity = new AnnotazioneSQLiteEntity(id, "nota", "1");
        AnnotazioneMetadataSQLiteEntity metaEntity = buildMetaEntity(id, "INSERITA");

        when(annotazioneRepository.findById(id.toString())).thenReturn(Optional.of(entity));
        when(metadataRepository.findById(id.toString())).thenReturn(Optional.of(metaEntity));

        Optional<AnnotazioneCompleta> result = service.trovaPerID(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getAnnotazione().getId());
    }

    @Test
    void trovaPerID_whenNotFound_returnsEmpty() {
        UUID id = UUID.randomUUID();
        when(annotazioneRepository.findById(id.toString())).thenReturn(Optional.empty());

        Optional<AnnotazioneCompleta> result = service.trovaPerID(id);

        assertTrue(result.isEmpty());
    }

    @Test
    void cambiaStato_whenFound_changesState() {
        UUID id = UUID.randomUUID();
        AnnotazioneSQLiteEntity entity = new AnnotazioneSQLiteEntity(id, "nota", "1");
        AnnotazioneMetadataSQLiteEntity metaEntity = buildMetaEntity(id, "INSERITA");

        when(metadataRepository.findById(id.toString())).thenReturn(Optional.of(metaEntity));
        when(annotazioneRepository.findById(id.toString())).thenReturn(Optional.of(entity));

        AnnotazioneCompleta result = service.cambiaStato(id, "MODIFICATA", "user");

        assertNotNull(result);
        verify(metadataRepository).save(any());
    }

    @Test
    void cambiaStato_withInvalidState_throwsIllegalArgument() {
        UUID id = UUID.randomUUID();
        AnnotazioneSQLiteEntity entity = new AnnotazioneSQLiteEntity(id, "nota", "1");
        AnnotazioneMetadataSQLiteEntity metaEntity = buildMetaEntity(id, "INSERITA");

        when(metadataRepository.findById(id.toString())).thenReturn(Optional.of(metaEntity));
        when(annotazioneRepository.findById(id.toString())).thenReturn(Optional.of(entity));

        assertThrows(IllegalArgumentException.class,
                () -> service.cambiaStato(id, "STATO_INESISTENTE", "user"));
    }

    @Test
    void cambiaStato_whenNotFound_throwsRuntimeException() {
        UUID id = UUID.randomUUID();
        when(metadataRepository.findById(id.toString())).thenReturn(Optional.empty());
        when(annotazioneRepository.findById(id.toString())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.cambiaStato(id, "MODIFICATA", "user"));
    }

    @Test
    void aggiornaAnnotazioneCompleta_whenFound_updatesAllFields() {
        UUID id = UUID.randomUUID();
        AnnotazioneSQLiteEntity entity = new AnnotazioneSQLiteEntity(id, "vecchio", "1");
        AnnotazioneMetadataSQLiteEntity metaEntity = buildMetaEntity(id, "INSERITA");

        when(annotazioneRepository.findById(id.toString())).thenReturn(Optional.of(entity));
        when(metadataRepository.findById(id.toString())).thenReturn(Optional.of(metaEntity));
        when(annotazioneRepository.save(any())).thenReturn(entity);
        when(metadataRepository.save(any())).thenReturn(metaEntity);

        AnnotazioneCompleta result = service.aggiornaAnnotazioneCompleta(
                id, "nuovo", "desc", "user", "cat", "tag1", true, 2);

        assertNotNull(result);
    }

    @Test
    void aggiornaAnnotazioneCompleta_whenNotFound_returnsNull() {
        UUID id = UUID.randomUUID();
        when(annotazioneRepository.findById(id.toString())).thenReturn(Optional.empty());

        AnnotazioneCompleta result = service.aggiornaAnnotazioneCompleta(
                id, "nuovo", "desc", "user", "cat", "tag1", true, 2);

        assertNull(result);
    }

    private AnnotazioneMetadataSQLiteEntity buildMetaEntity(UUID id, String stato) {
        AnnotazioneMetadata meta = new AnnotazioneMetadata();
        meta.setId(id);
        meta.setVersioneNota("1");
        meta.setUtenteCreazione("user");
        meta.setStato(stato);
        return new AnnotazioneMetadataSQLiteEntity(meta);
    }
}
