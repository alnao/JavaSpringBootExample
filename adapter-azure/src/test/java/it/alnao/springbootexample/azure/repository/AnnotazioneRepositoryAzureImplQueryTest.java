package it.alnao.springbootexample.azure.repository;

import it.alnao.springbootexample.azure.entity.AnnotazioneCosmosEntity;
import it.alnao.springbootexample.core.domain.Annotazione;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Copre le query non verificate in {@link AnnotazioneRepositoryAzureImplTest}.
 */
class AnnotazioneRepositoryAzureImplQueryTest {

    @Mock AnnotazioneCosmosRepository cosmosRepository;
    @InjectMocks AnnotazioneRepositoryAzureImpl repository;

    private AnnotazioneCosmosEntity entity;
    private UUID id;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        id = UUID.randomUUID();
        entity = new AnnotazioneCosmosEntity();
        entity.setId(id.toString());
        entity.setVersioneNota("1.0");
        entity.setValoreNota("il testo della nota");
    }

    @Test
    void findByValoreNotaContaining_delegatesToTheCaseInsensitiveQuery() {
        when(cosmosRepository.findByValoreNotaContainingIgnoreCase("testo")).thenReturn(List.of(entity));
        List<Annotazione> result = repository.findByValoreNotaContaining("testo");
        assertEquals(1, result.size());
        assertEquals(id, result.get(0).getId());
        verify(cosmosRepository).findByValoreNotaContainingIgnoreCase("testo");
    }

    @Test
    void findByValoreNotaContaining_whenNoMatch_returnsEmptyList() {
        when(cosmosRepository.findByValoreNotaContainingIgnoreCase("assente")).thenReturn(List.of());
        assertTrue(repository.findByValoreNotaContaining("assente").isEmpty());
    }

    @Test
    void findByVersioneNota_filtersTheScannedEntities() {
        AnnotazioneCosmosEntity altra = new AnnotazioneCosmosEntity();
        altra.setId(UUID.randomUUID().toString());
        altra.setVersioneNota("2.0");
        altra.setValoreNota("altro testo");
        when(cosmosRepository.findAll()).thenReturn(List.of(entity, altra));

        List<Annotazione> result = repository.findByVersioneNota("1.0");

        assertEquals(1, result.size());
        assertEquals("1.0", result.get(0).getVersioneNota());
    }

    @Test
    void findByVersioneNota_whenNoMatch_returnsEmptyList() {
        when(cosmosRepository.findAll()).thenReturn(List.of(entity));
        assertTrue(repository.findByVersioneNota("9.9").isEmpty());
    }

    @Test
    void count_delegates() {
        when(cosmosRepository.count()).thenReturn(8L);
        assertEquals(8L, repository.count());
    }
}
