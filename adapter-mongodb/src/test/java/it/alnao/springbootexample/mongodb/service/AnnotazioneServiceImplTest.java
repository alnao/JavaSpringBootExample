package it.alnao.springbootexample.mongodb.service;

import it.alnao.springbootexample.core.domain.Annotazione;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.repository.AnnotazioneMetadataRepository;
import it.alnao.springbootexample.core.repository.AnnotazioneRepository;
import it.alnao.springbootexample.core.service.AnnotazioneLockService;
import it.alnao.springbootexample.mongodb.entity.AnnotazioneStoricoEntity;
import it.alnao.springbootexample.mongodb.repository.AnnotazioneStoricoMongoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AnnotazioneServiceImplTest {

    @Mock AnnotazioneRepository annotazioneRepository;
    @Mock AnnotazioneMetadataRepository metadataRepository;
    @Mock AnnotazioneStoricoMongoRepository storicoMongoRepository;
    @Mock AnnotazioneLockService lockService;
    @InjectMocks AnnotazioneServiceImpl service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void creaAnnotazione_savesAndReturnsResult() {
        UUID id = UUID.randomUUID();
        Annotazione ann = new Annotazione(id, "v1.0", "nota");
        AnnotazioneMetadata meta = buildMetadata(id);
        when(annotazioneRepository.save(any())).thenReturn(ann);
        when(metadataRepository.save(any())).thenReturn(meta);

        AnnotazioneCompleta result = service.creaAnnotazione("nota", "desc", "user");

        assertNotNull(result);
        assertEquals("nota", result.getAnnotazione().getValoreNota());
        verify(annotazioneRepository).save(any());
        verify(metadataRepository).save(any());
    }

    @Test
    void aggiornaAnnotazione_whenFound_acquiresLockAndUpdates() {
        UUID id = UUID.randomUUID();
        Annotazione ann = new Annotazione(id, "v1.0", "vecchio");
        AnnotazioneMetadata meta = buildMetadata(id);
        when(annotazioneRepository.findById(id)).thenReturn(Optional.of(ann));
        when(metadataRepository.findById(id)).thenReturn(Optional.of(meta));
        when(lockService.acquireLock(eq(id), eq("user"), anyLong())).thenReturn(true);
        when(annotazioneRepository.save(any())).thenReturn(ann);
        when(metadataRepository.save(any())).thenReturn(meta);
        when(storicoMongoRepository.save(any())).thenReturn(new AnnotazioneStoricoEntity());

        AnnotazioneCompleta result = service.aggiornaAnnotazione(id, "nuovo", "nuova desc", "user");

        assertNotNull(result);
        verify(lockService).acquireLock(eq(id), eq("user"), anyLong());
        verify(lockService).releaseLock(id, "user");
    }

    @Test
    void aggiornaAnnotazione_whenLockHeldByOther_throwsException() {
        UUID id = UUID.randomUUID();
        Annotazione ann = new Annotazione(id, "v1.0", "vecchio");
        AnnotazioneMetadata meta = buildMetadata(id);
        when(annotazioneRepository.findById(id)).thenReturn(Optional.of(ann));
        when(metadataRepository.findById(id)).thenReturn(Optional.of(meta));
        when(lockService.acquireLock(eq(id), eq("user"), anyLong())).thenReturn(false);
        when(lockService.isLocked(id)).thenReturn(true);
        when(lockService.getOwner(id)).thenReturn(Optional.of("otherUser"));

        assertThrows(Exception.class,
                () -> service.aggiornaAnnotazione(id, "nuovo", "desc", "user"));
    }

    @Test
    void getAnnotazioneRepository_returnsInjectedRepo() {
        assertSame(annotazioneRepository, service.getAnnotazioneRepository());
    }

    @Test
    void getMetadataRepository_returnsInjectedRepo() {
        assertSame(metadataRepository, service.getMetadataRepository());
    }

    private AnnotazioneMetadata buildMetadata(UUID id) {
        AnnotazioneMetadata m = new AnnotazioneMetadata();
        m.setId(id);
        m.setVersioneNota("v1.0");
        m.setUtenteCreazione("user");
        m.setDataInserimento(LocalDateTime.now());
        m.setDataUltimaModifica(LocalDateTime.now());
        m.setStato("INSERITA");
        return m;
    }
}
