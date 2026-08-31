package it.alnao.springbootexample.azure.service;

import it.alnao.springbootexample.azure.repository.AnnotazioneMetadataRepositoryAzureImpl;
import it.alnao.springbootexample.azure.repository.AnnotazioneRepositoryAzureImpl;
import it.alnao.springbootexample.core.domain.Annotazione;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AnnotazioneServiceAzureImplTest {

    @Mock AnnotazioneRepositoryAzureImpl annotazioneRepository;
    @Mock AnnotazioneMetadataRepositoryAzureImpl metadataRepository;

    AnnotazioneServiceAzureImpl service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new AnnotazioneServiceAzureImpl(annotazioneRepository, metadataRepository);
    }

    @Test
    void creaAnnotazione_savesAndReturnsResult() {
        UUID id = UUID.randomUUID();
        Annotazione annotazione = new Annotazione(id, "1.0", "nota azure");
        AnnotazioneMetadata metadata = buildMetadata(id);
        when(annotazioneRepository.save(any())).thenReturn(annotazione);
        when(metadataRepository.save(any())).thenReturn(metadata);

        AnnotazioneCompleta result = service.creaAnnotazione("nota azure", "desc", "user");

        assertNotNull(result);
        assertEquals("nota azure", result.getAnnotazione().getValoreNota());
        verify(annotazioneRepository).save(any());
        verify(metadataRepository).save(any());
    }

    @Test
    void aggiornaAnnotazione_whenFound_updatesAndReturns() {
        UUID id = UUID.randomUUID();
        Annotazione annotazione = new Annotazione(id, "1.0", "vecchio");
        AnnotazioneMetadata metadata = buildMetadata(id);
        when(annotazioneRepository.findById(id)).thenReturn(Optional.of(annotazione));
        when(metadataRepository.findById(id)).thenReturn(Optional.of(metadata));
        when(annotazioneRepository.save(any())).thenReturn(annotazione);
        when(metadataRepository.save(any())).thenReturn(metadata);

        AnnotazioneCompleta result = service.aggiornaAnnotazione(id, "nuovo", "nuova desc", "user");

        assertNotNull(result);
        verify(annotazioneRepository).save(any());
    }

    @Test
    void aggiornaAnnotazione_whenNotFound_throwsOrReturnsNull() {
        UUID id = UUID.randomUUID();
        when(annotazioneRepository.findById(id)).thenReturn(Optional.empty());
        when(metadataRepository.findById(id)).thenReturn(Optional.empty());

        // Azure impl throws RuntimeException when annotation not found
        assertThrows(RuntimeException.class,
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
        m.setVersioneNota("1.0");
        m.setUtenteCreazione("user");
        m.setDataInserimento(LocalDateTime.now());
        m.setDataUltimaModifica(LocalDateTime.now());
        m.setStato("INSERITA");
        return m;
    }
}
