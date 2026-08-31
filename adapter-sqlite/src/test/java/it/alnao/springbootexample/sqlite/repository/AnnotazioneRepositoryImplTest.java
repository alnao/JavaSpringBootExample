package it.alnao.springbootexample.sqlite.repository;

import it.alnao.springbootexample.core.domain.Annotazione;
import it.alnao.springbootexample.sqlite.entity.AnnotazioneSQLiteEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AnnotazioneRepositoryImplTest {

    @Mock AnnotazioneSQLiteJpaRepository jpaRepository;
    @InjectMocks AnnotazioneRepositoryImpl repository;

    private UUID id;
    private AnnotazioneSQLiteEntity entity;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        id = UUID.randomUUID();
        entity = new AnnotazioneSQLiteEntity(id, "il testo della nota", "1.0");
    }

    @Test
    void save_mapsDomainToEntityAndBack() {
        Annotazione annotazione = new Annotazione(id, "1.0", "il testo della nota");
        when(jpaRepository.save(any(AnnotazioneSQLiteEntity.class))).thenReturn(entity);
        Annotazione saved = repository.save(annotazione);
        assertEquals("il testo della nota", saved.getValoreNota());
        verify(jpaRepository).save(any(AnnotazioneSQLiteEntity.class));
    }

    @Test
    void findById_convertsUuidToStringKey() {
        when(jpaRepository.findById(id.toString())).thenReturn(Optional.of(entity));
        Optional<Annotazione> result = repository.findById(id);
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
    void findAll_mapsEveryEntity() {
        when(jpaRepository.findAll()).thenReturn(List.of(entity, entity));
        assertEquals(2, repository.findAll().size());
    }

    @Test
    void findByVersioneNota_delegatesAndMaps() {
        when(jpaRepository.findByVersioneNota("1.0")).thenReturn(List.of(entity));
        List<Annotazione> result = repository.findByVersioneNota("1.0");
        assertEquals(1, result.size());
        assertEquals("1.0", result.get(0).getVersioneNota());
    }

    @Test
    void findByValoreNotaContaining_delegatesAndMaps() {
        when(jpaRepository.findByValoreNotaContaining("testo")).thenReturn(List.of(entity));
        assertEquals(1, repository.findByValoreNotaContaining("testo").size());
    }

    @Test
    void findByValoreNotaContaining_whenNoMatch_returnsEmpty() {
        when(jpaRepository.findByValoreNotaContaining("assente")).thenReturn(List.of());
        assertTrue(repository.findByValoreNotaContaining("assente").isEmpty());
    }

    @Test
    void deleteById_convertsUuidToStringKey() {
        repository.deleteById(id);
        verify(jpaRepository).deleteById(id.toString());
    }

    @Test
    void existsById_convertsUuidToStringKey() {
        when(jpaRepository.existsById(id.toString())).thenReturn(true);
        assertTrue(repository.existsById(id));
    }

    @Test
    void count_delegates() {
        when(jpaRepository.count()).thenReturn(4L);
        assertEquals(4L, repository.count());
    }
}
