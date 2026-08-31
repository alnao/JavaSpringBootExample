package it.alnao.springbootexample.mongodb.service;

import it.alnao.springbootexample.core.domain.Annotazione;
import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.exception.AnnotationLockedException;
import it.alnao.springbootexample.core.repository.AnnotazioneMetadataRepository;
import it.alnao.springbootexample.core.repository.AnnotazioneRepository;
import it.alnao.springbootexample.core.service.AnnotazioneLockService;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Copre i rami di gestione lock e validazione input di aggiornaAnnotazione.
 */
class AnnotazioneServiceImplLockTest {

    @Mock AnnotazioneRepository annotazioneRepository;
    @Mock AnnotazioneMetadataRepository metadataRepository;
    @Mock AnnotazioneStoricoMongoRepository storicoMongoRepository;
    @Mock AnnotazioneLockService lockService;
    @InjectMocks AnnotazioneServiceImpl service;

    private UUID id;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        id = UUID.randomUUID();
    }

    private void esistente() {
        Annotazione ann = new Annotazione(id, "1.0", "vecchio testo");
        AnnotazioneMetadata meta = new AnnotazioneMetadata(id, "1.0", "mario", "descrizione");
        meta.setDataUltimaModifica(LocalDateTime.now().minusDays(1));
        lenient().when(annotazioneRepository.findById(id)).thenReturn(Optional.of(ann));
        lenient().when(metadataRepository.findById(id)).thenReturn(Optional.of(meta));
        lenient().when(annotazioneRepository.save(any(Annotazione.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(metadataRepository.save(any(AnnotazioneMetadata.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void aggiornaAnnotazione_withNullUser_throwsIllegalArgument() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.aggiornaAnnotazione(id, "nuovo", "descr", null));
        assertEquals("Utente non può essere null", e.getMessage());
        verifyNoInteractions(lockService);
    }

    @Test
    void aggiornaAnnotazione_whenLockIsAlreadyHeldByTheSameUser_proceeds() {
        esistente();
        when(lockService.acquireLock(any(UUID.class), anyString(), anyLong())).thenReturn(false);
        when(lockService.getOwner(id)).thenReturn(Optional.of("mario"));

        assertDoesNotThrow(() -> service.aggiornaAnnotazione(id, "nuovo", "descr", "mario"));
    }

    @Test
    void aggiornaAnnotazione_whenLockIsHeldByAnother_throwsAnnotationLocked() {
        when(lockService.acquireLock(any(UUID.class), anyString(), anyLong())).thenReturn(false);
        when(lockService.getOwner(id)).thenReturn(Optional.of("luigi"));

        assertThrows(AnnotationLockedException.class,
                () -> service.aggiornaAnnotazione(id, "nuovo", "descr", "mario"));
    }

    @Test
    void aggiornaAnnotazione_whenLockHasNoOwner_throwsAnnotationLocked() {
        when(lockService.acquireLock(any(UUID.class), anyString(), anyLong())).thenReturn(false);
        when(lockService.getOwner(id)).thenReturn(Optional.empty());

        assertThrows(AnnotationLockedException.class,
                () -> service.aggiornaAnnotazione(id, "nuovo", "descr", "mario"));
    }
}
