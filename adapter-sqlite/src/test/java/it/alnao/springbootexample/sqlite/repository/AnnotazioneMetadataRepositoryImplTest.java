package it.alnao.springbootexample.sqlite.repository;

import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import it.alnao.springbootexample.sqlite.entity.AnnotazioneMetadataSQLiteEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AnnotazioneMetadataRepositoryImplTest {

    @Mock AnnotazioneMetadataSQLiteJpaRepository jpaRepository;
    @InjectMocks AnnotazioneMetadataRepositoryImpl repository;

    private UUID id;
    private AnnotazioneMetadataSQLiteEntity entity;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        id = UUID.randomUUID();
        AnnotazioneMetadata metadata = new AnnotazioneMetadata(id, "1.0", "mario", "una descrizione");
        metadata.setCategoria("lavoro");
        metadata.setTags("urgente,nota");
        metadata.setPubblica(true);
        metadata.setPriorita(3);
        metadata.setStato(StatoAnnotazione.DAINVIARE.getValue());
        entity = new AnnotazioneMetadataSQLiteEntity(metadata);
    }

    @Test
    void findByStato_passesEnumNameAndMaps() {
        when(jpaRepository.findByStato(StatoAnnotazione.DAINVIARE.name())).thenReturn(List.of(entity));
        List<AnnotazioneMetadata> result = repository.findByStato(StatoAnnotazione.DAINVIARE);
        assertEquals(1, result.size());
        assertEquals(id, result.get(0).getId());
        verify(jpaRepository).findByStato(StatoAnnotazione.DAINVIARE.name());
    }

    @Test
    void findByDataInserimentoBetween_delegatesWithBounds() {
        LocalDateTime inizio = LocalDateTime.now().minusDays(1);
        LocalDateTime fine = LocalDateTime.now();
        when(jpaRepository.findByDataInserimentoBetween(inizio, fine)).thenReturn(List.of(entity));
        assertEquals(1, repository.findByDataInserimentoBetween(inizio, fine).size());
        verify(jpaRepository).findByDataInserimentoBetween(inizio, fine);
    }

    @Test
    void findByUtenteCreazione_delegatesAndMaps() {
        when(jpaRepository.findByUtenteCreazione("mario")).thenReturn(List.of(entity));
        List<AnnotazioneMetadata> result = repository.findByUtenteCreazione("mario");
        assertEquals("mario", result.get(0).getUtenteCreazione());
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
    void findByDescrizioneContaining_delegatesAndMaps() {
        when(jpaRepository.findByDescrizioneContaining("descr")).thenReturn(List.of(entity));
        assertEquals(1, repository.findByDescrizioneContaining("descr").size());
    }

    @Test
    void findByTagsContaining_delegatesAndMaps() {
        when(jpaRepository.findByTagsContaining("urgente")).thenReturn(List.of(entity));
        assertEquals(1, repository.findByTagsContaining("urgente").size());
    }

    @Test
    void findByStato_whenNoRows_returnsEmptyList() {
        when(jpaRepository.findByStato(StatoAnnotazione.INVIATA.name())).thenReturn(List.of());
        assertTrue(repository.findByStato(StatoAnnotazione.INVIATA).isEmpty());
    }

    @Test
    void countByUtenteCreazione_delegates() {
        when(jpaRepository.countByUtenteCreazione("mario")).thenReturn(5L);
        assertEquals(5L, repository.countByUtenteCreazione("mario"));
    }

    @Test
    void save_mapsDomainToEntityAndBack() {
        AnnotazioneMetadata metadata = new AnnotazioneMetadata(id, "1.0", "mario", "descr");
        when(jpaRepository.save(any(AnnotazioneMetadataSQLiteEntity.class))).thenReturn(entity);
        AnnotazioneMetadata saved = repository.save(metadata);
        assertEquals(id, saved.getId());
        verify(jpaRepository).save(any(AnnotazioneMetadataSQLiteEntity.class));
    }

    @Test
    void findById_convertsUuidToStringKey() {
        when(jpaRepository.findById(id.toString())).thenReturn(Optional.of(entity));
        Optional<AnnotazioneMetadata> result = repository.findById(id);
        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    void findById_whenMissing_returnsEmpty() {
        UUID other = UUID.randomUUID();
        when(jpaRepository.findById(other.toString())).thenReturn(Optional.empty());
        assertTrue(repository.findById(other).isEmpty());
    }

    @Test
    void existsById_convertsUuidToStringKey() {
        when(jpaRepository.existsById(id.toString())).thenReturn(true);
        assertTrue(repository.existsById(id));
    }

    @Test
    void findAll_mapsEveryEntity() {
        when(jpaRepository.findAll()).thenReturn(List.of(entity, entity));
        assertEquals(2, repository.findAll().size());
    }

    @Test
    void count_delegates() {
        when(jpaRepository.count()).thenReturn(9L);
        assertEquals(9L, repository.count());
    }

    @Test
    void deleteById_convertsUuidToStringKey() {
        repository.deleteById(id);
        verify(jpaRepository).deleteById(id.toString());
    }
}
