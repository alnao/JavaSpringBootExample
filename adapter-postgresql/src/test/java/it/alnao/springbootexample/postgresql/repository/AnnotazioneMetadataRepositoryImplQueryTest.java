package it.alnao.springbootexample.postgresql.repository;

import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.postgresql.entity.AnnotazioneMetadataEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Copre le query di ricerca non verificate in {@link AnnotazioneMetadataRepositoryImplTest}.
 */
class AnnotazioneMetadataRepositoryImplQueryTest {

    @Mock AnnotazioneMetadataJpaRepository jpaRepository;
    @InjectMocks AnnotazioneMetadataRepositoryImpl repository;

    private AnnotazioneMetadataEntity entity;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        entity = new AnnotazioneMetadataEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setVersioneNota("1.0");
        entity.setUtenteCreazione("mario");
        entity.setDescrizione("una descrizione");
        entity.setCategoria("lavoro");
        entity.setTags("urgente");
        entity.setPubblica(true);
        entity.setPriorita(3);
        entity.setStato("INSERITA");
    }

    @Test
    void findByUtenteCreazione_delegatesAndMaps() {
        when(jpaRepository.findByUtenteCreazione("mario")).thenReturn(List.of(entity));
        List<AnnotazioneMetadata> result = repository.findByUtenteCreazione("mario");
        assertEquals(1, result.size());
        assertEquals("mario", result.get(0).getUtenteCreazione());
    }

    @Test
    void findByDescrizioneContaining_usesTheCaseInsensitiveQuery() {
        when(jpaRepository.findByDescrizioneContainingIgnoreCase("DESCRIZIONE")).thenReturn(List.of(entity));
        assertEquals(1, repository.findByDescrizioneContaining("DESCRIZIONE").size());
        verify(jpaRepository).findByDescrizioneContainingIgnoreCase("DESCRIZIONE");
    }

    @Test
    void findByCategoria_delegatesAndMaps() {
        when(jpaRepository.findByCategoria("lavoro")).thenReturn(List.of(entity));
        assertEquals("lavoro", repository.findByCategoria("lavoro").get(0).getCategoria());
    }

    @Test
    void findByPubblica_delegatesAndMaps() {
        when(jpaRepository.findByPubblica(true)).thenReturn(List.of(entity));
        assertEquals(1, repository.findByPubblica(true).size());
    }

    @Test
    void findByPriorita_delegatesAndMaps() {
        when(jpaRepository.findByPriorita(3)).thenReturn(List.of(entity));
        assertEquals(3, repository.findByPriorita(3).get(0).getPriorita());
    }

    @Test
    void findByTagsContaining_delegatesAndMaps() {
        when(jpaRepository.findByTagsContaining("urgente")).thenReturn(List.of(entity));
        assertEquals(1, repository.findByTagsContaining("urgente").size());
    }

    @Test
    void findByDataInserimentoBetween_delegatesWithBounds() {
        LocalDateTime inizio = LocalDateTime.now().minusDays(1);
        LocalDateTime fine = LocalDateTime.now();
        when(jpaRepository.findByDataInserimentoBetween(inizio, fine)).thenReturn(List.of(entity));
        assertEquals(1, repository.findByDataInserimentoBetween(inizio, fine).size());
    }

    @Test
    void findWithFilters_passesEveryFilterThrough() {
        when(jpaRepository.findWithFilters("descr", "lavoro", true, 3)).thenReturn(List.of(entity));
        assertEquals(1, repository.findWithFilters("descr", "lavoro", true, 3).size());
        verify(jpaRepository).findWithFilters("descr", "lavoro", true, 3);
    }

    @Test
    void findWithFilters_acceptsNullFilters() {
        when(jpaRepository.findWithFilters(null, null, null, null)).thenReturn(List.of(entity, entity));
        assertEquals(2, repository.findWithFilters(null, null, null, null).size());
    }

    @Test
    void countByUtenteCreazione_delegates() {
        when(jpaRepository.countByUtenteCreazione("mario")).thenReturn(4L);
        assertEquals(4L, repository.countByUtenteCreazione("mario"));
    }

    @Test
    void findByUtenteCreazione_whenNoRows_returnsEmptyList() {
        when(jpaRepository.findByUtenteCreazione("fantasma")).thenReturn(List.of());
        assertTrue(repository.findByUtenteCreazione("fantasma").isEmpty());
    }
}
